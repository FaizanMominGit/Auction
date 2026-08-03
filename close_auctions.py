import firebase_admin
from firebase_admin import credentials, firestore, auth
from google.cloud.firestore_v1.base_query import FieldFilter
import pytz
from datetime import datetime

# Initialize Firebase
cred_path = "auction-6a02c-firebase-adminsdk-fbsvc-4cc5dd13d3.json"
cred = credentials.Certificate(cred_path)
firebase_admin.initialize_app(cred)

db = firestore.client()

def close_expired_auctions():
    """Closes expired auctions and updates user balances, handles live and scheduled."""
    try:
        ist = datetime.now(pytz.timezone("Asia/Kolkata"))
        auction_items_ref = db.collection('auctionItems')

        # Query for both 'live' and 'scheduled' auctions
        query = auction_items_ref.where(filter=FieldFilter('status', 'in', ['live', 'scheduled']))
        snapshot = query.get()

        batch = db.batch()

        for doc in snapshot:
            auction_data = doc.to_dict()
            end_date = auction_data.get('endDate')
            end_time = auction_data.get('endTime')
            user_id = auction_data.get('userId')
            highest_bid = auction_data.get('highestBid')
            starting_price = auction_data.get('startingPrice')
            status = auction_data.get('status')

            if end_date and end_time and status == 'live':
                try:
                    end_date_time_str = f"{end_date} {end_time}"
                    end_date_time = datetime.strptime(end_date_time_str, '%d/%m/%Y %I:%M %p')
                    end_date_time = pytz.timezone("Asia/Kolkata").localize(end_date_time)

                    if ist > end_date_time:
                        doc_ref = auction_items_ref.document(doc.id)
                        batch.update(doc_ref, {'status': 'closed'})
                        print(f"Auction {doc.id} closed.")

                        bid_amount = highest_bid if highest_bid is not None else starting_price
                        if bid_amount is not None:
                            user_ref = db.collection('users').document(user_id)
                            user_doc = user_ref.get()

                            if user_doc.exists:
                                batch.update(user_ref, {'totalBalance': firestore.Increment(bid_amount)})
                                print(f"User {user_id}'s totalBalance increased by {bid_amount}.")
                            else:
                                fallback_user_id = "vCQTuiiTgagqGjxX8xlwKIcqMVH2"  # Replace with your fallback user ID
                                fallback_user_ref = db.collection('users').document(fallback_user_id)
                                batch.update(fallback_user_ref, {'totalBalance': firestore.Increment(bid_amount)})
                                print(f"User {user_id} does not exist. Amount {bid_amount} added to fallback user {fallback_user_id}.")

                except ValueError as e:
                    print(f"Error parsing date/time for document {doc.id}: {e}")

            elif status == 'scheduled' and auction_data.get('startDate') and auction_data.get('startTime'):
                start_date_time_str = f"{auction_data['startDate']} {auction_data['startTime']}"
                start_date_time = datetime.strptime(start_date_time_str, '%d/%m/%Y %I:%M %p')
                start_date_time = pytz.timezone("Asia/Kolkata").localize(start_date_time)

                if ist >= start_date_time:
                    doc_ref = auction_items_ref.document(doc.id)
                    batch.update(doc_ref, {'status': 'live'})
                    print(f"Auction {doc.id} started and status updated to live.")

        try:
            batch.commit()
            print("Auctions successfully updated.")
        except Exception as e:
            print(f"Batch commit failed: {e}")

    except Exception as error:
        print(f"Error closing expired auctions: {error}")

def delete_users():
    """Deletes users listed in the users_to_delete collection from Firebase Authentication (with batching)."""
    try:
        users_to_delete_ref = db.collection('users_to_delete')
        snapshot = users_to_delete_ref.get()

        batch = db.batch()  # Create a batch
        deleted_count = 0

        for doc in snapshot:
            user_data = doc.to_dict()
            user_id = user_data.get('userId')

            if user_id:
                try:
                    # Delete from Firebase Authentication
                    auth.delete_user(user_id)
                    print(f"User {user_id} deleted from Authentication.")

                    # Delete user from 'users' collection
                    user_doc_ref = db.collection('users').document(user_id)
                    batch.delete(user_doc_ref)
                    print(f"User {user_id} deleted from users collection.")

                    # Delete user document from 'users_to_delete' collection
                    batch.delete(users_to_delete_ref.document(doc.id))
                    deleted_count += 1
                    print(f"User {user_id} marked for deletion from users_to_delete collection.")

                except auth.UserNotFoundError:
                    print(f"User {user_id} not found in Authentication.")

                    # Still delete from Firestore users & users_to_delete collections
                    user_doc_ref = db.collection('users').document(user_id)
                    batch.delete(user_doc_ref)
                    batch.delete(users_to_delete_ref.document(doc.id))
                    deleted_count += 1

                except Exception as e:
                    print(f"Error deleting user {user_id}: {e}")

            else:
                print("userId field is missing in a document in 'users_to_delete'.")

        if deleted_count > 0:
            try:
                batch.commit()  # Commit the batch
                print(f"{deleted_count} users successfully deleted from users and users_to_delete collections.")
            except Exception as e:
                print(f"Error committing batch delete: {e}")
        else:
            print("No users to delete.")

    except Exception as e:
        print(f"Error fetching users to delete: {e}")

# Run the functions
close_expired_auctions()
delete_users()