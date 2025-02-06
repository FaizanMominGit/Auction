import firebase_admin
from firebase_admin import credentials, firestore
import pytz
from datetime import datetime

# Initialize Firebase
cred_path = "auction-d7ab9-firebase-adminsdk-fbsvc-0667fe910d.json"
cred = credentials.Certificate(cred_path)
firebase_admin.initialize_app(cred)

db = firestore.client()

def close_expired_auctions():
    try:
        # Get current time in IST
        ist = datetime.now(pytz.timezone("Asia/Kolkata"))

        # Reference to auctionItems collection
        auction_items_ref = db.collection('auctionItems')
        snapshot = auction_items_ref.get()

        batch = db.batch()

        for doc in snapshot:
            auction_data = doc.to_dict()
            end_date = auction_data.get('endDate')
            end_time = auction_data.get('endTime')
            status = auction_data.get('status')
            user_id = auction_data.get('userId')
            highest_bid = auction_data.get('highestBid')
            starting_price = auction_data.get('startingPrice')

            if end_date and end_time:
                try:
                    # Convert endDate and endTime to datetime
                    end_date_time_str = f"{end_date} {end_time}"
                    end_date_time = datetime.strptime(end_date_time_str, '%d/%m/%Y %I:%M %p')
                    end_date_time = pytz.timezone("Asia/Kolkata").localize(end_date_time)

                    # Check if auction has expired
                    if ist > end_date_time:
                        doc_ref = auction_items_ref.document(doc.id)
                        batch.update(doc_ref, {'status': 'closed'})
                        print(f"Auction {doc.id} closed.")

                        # Determine the amount to add
                        bid_amount = highest_bid if highest_bid is not None else starting_price
                        if bid_amount is not None:
                            user_ref = db.collection('users').document(user_id)
                            user_doc = user_ref.get()

                            if user_doc.exists:
                                batch.update(user_ref, {'totalBalance': firestore.Increment(bid_amount)})
                                print(f"User {user_id}'s totalBalance increased by {bid_amount}.")
                            else:
                                # Fallback user ID if the original user does not exist
                                fallback_user_id = "vCQTuiiTgagqGjxX8xlwKIcqMVH2"
                                fallback_user_ref = db.collection('users').document(fallback_user_id)
                                batch.update(fallback_user_ref, {'totalBalance': firestore.Increment(bid_amount)})
                                print(f"User {user_id} does not exist. Amount {bid_amount} added to fallback user {fallback_user_id}.")

                    # Check if auction is scheduled and start time has passed
                    if status == 'scheduled' and auction_data.get('startDate') and auction_data.get('startTime'):
                        start_date_time_str = f"{auction_data['startDate']} {auction_data['startTime']}"
                        start_date_time = datetime.strptime(start_date_time_str, '%d/%m/%Y %I:%M %p')
                        start_date_time = pytz.timezone("Asia/Kolkata").localize(start_date_time)

                        if ist >= start_date_time:
                            doc_ref = auction_items_ref.document(doc.id)
                            batch.update(doc_ref, {'status': 'live'})
                            print(f"Auction {doc.id} started and status updated to live.")

                except ValueError as e:
                    print(f"Error parsing date/time for document {doc.id}: {e}")

        # Commit batch updates
        try:
            batch.commit()
            print("Expired and live auctions successfully updated.")
        except Exception as e:
            print(f"Batch commit failed: {e}")

    except Exception as error:
        print(f"Error closing expired auctions: {error}")

# Run the function
close_expired_auctions()
