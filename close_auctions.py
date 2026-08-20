import firebase_admin
from firebase_admin import credentials, firestore, auth
import pytz
from datetime import datetime

FALLBACK_USER_ID = "E3dxgsx4JwhvSpp30BiRU5nkUDe2"
DATE_TIME_FORMAT = '%d/%m/%Y %I:%M %p'

# Use the specific JSON key filename you provided earlier
cred_path = "auction-6a02c-firebase-adminsdk-fbsvc-4cc5dd13d3.json"
cred = credentials.Certificate(cred_path)
firebase_admin.initialize_app(cred)

db = firestore.client()

def close_expired_auctions():
    try:
        ist = datetime.now(pytz.timezone("Asia/Kolkata"))
        auction_items_ref = db.collection('auctionItems')
        # Use modern FieldFilter to avoid warnings
        query = auction_items_ref.where(filter=firestore.FieldFilter('status', '==', 'live'))
        snapshot = query.get()
        batch = db.batch()

        server_doc = db.collection('server').document('server').get()
        if not server_doc.exists or not server_doc.to_dict().get('isServer', False):
            print("Unauthorized: Server update not allowed.")
            return

        for doc in snapshot:
            auction_data = doc.to_dict()
            seller_id = auction_data.get('userId')
            item_title = auction_data.get('title')
            end_date = auction_data.get('endDate')
            end_time = auction_data.get('endTime')
            highest_bid = auction_data.get('highestBid')
            highest_bidder = auction_data.get('highestBidder')
            starting_price = auction_data.get('startingPrice')

            if end_date and end_time:
                try:
                    end_date_time_str = f"{end_date} {end_time}"
                    end_date_time = datetime.strptime(end_date_time_str, DATE_TIME_FORMAT)
                    end_date_time = pytz.timezone("Asia/Kolkata").localize(end_date_time)

                    if ist > end_date_time:
                        doc_ref = auction_items_ref.document(doc.id)
                        updates = {'status': 'closed'}

                        if highest_bidder:
                            bid_amount = highest_bid if highest_bid is not None else starting_price
                            if bid_amount is not None:
                                winner_ref = db.collection('users').document(highest_bidder)
                                seller_ref = db.collection('users').document(seller_id)

                                winner_doc = winner_ref.get()
                                if winner_doc.exists:
                                    updates['highestBid'] = bid_amount
                                    batch.update(winner_ref, {
                                        'totalBalance': firestore.Increment(-bid_amount),
                                        'utilisedBalance': firestore.Increment(-bid_amount)
                                    })

                                    seller_doc = seller_ref.get()
                                    if seller_doc.exists:
                                        batch.update(seller_ref, {'totalBalance': firestore.Increment(bid_amount)})
                                    else:
                                        batch.update(db.collection('users').document(FALLBACK_USER_ID), {'totalBalance': firestore.Increment(bid_amount)})
                                else:
                                    fallback_ref = db.collection('users').document(FALLBACK_USER_ID)
                                    fallback_doc = fallback_ref.get()
                                    if fallback_doc.exists:
                                        batch.update(fallback_ref, {'totalBalance': firestore.Increment(bid_amount)})
                                    else:
                                        print(f"Fallback user {FALLBACK_USER_ID} not found!")

                        batch.update(doc_ref, updates)
                        print(f"Auction {doc.id} ({item_title}) closed.")
                except ValueError as e:
                    print(f"Error parsing date/time for document {doc.id}: {e}")

        try:
            batch.commit()
            print("Auctions update batch committed.")
        except Exception as e:
            print(f"Batch commit failed: {e}")

    except Exception as error:
        print(f"Error closing expired auctions: {error}")

def delete_users():
    try:
        users_to_delete_ref = db.collection('users_to_delete')
        snapshot = users_to_delete_ref.get()
        batch = db.batch()
        deleted_count = 0

        server_doc = db.collection('server').document('server').get()
        if not server_doc.exists or not server_doc.to_dict().get('isServer', False):
            print("Unauthorized: Server update not allowed.")
            return

        for doc in snapshot:
            user_data = doc.to_dict()
            user_id = user_data.get('userId')

            if user_id:
                try:
                    auth.delete_user(user_id)
                    user_doc_ref = db.collection('users').document(user_id)
                    batch.delete(user_doc_ref)
                    batch.delete(users_to_delete_ref.document(doc.id))
                    deleted_count += 1
                except auth.UserNotFoundError:
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
                batch.commit()
                print(f"{deleted_count} users successfully deleted.")
            except Exception as e:
                print(f"Error committing batch delete: {e}")
        else:
            print("No users to delete.")
    except Exception as e:
        print(f"Error fetching users to delete: {e}")

close_expired_auctions()
delete_users()
