const express = require('express');
const admin = require('firebase-admin');
const bodyParser = require('body-parser');
const cors = require('cors');

// Initialize Firebase Admin SDK
const serviceAccount = require('./path/to/serviceAccountKey.json'); // Replace with your service account key path

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});

// Create an Express application
const app = express();
app.use(cors());
app.use(bodyParser.json());

// Endpoint to delete a user by UID
app.delete('/deleteUser/:uid', async (req, res) => {
    const uid = req.params.uid;

    try {
        // Delete user from Firebase Authentication
        await admin.auth().deleteUser(uid);
        console.log(`Successfully deleted user with UID: ${uid}`);

        // Optionally, delete user data from Firestore if needed
        // await admin.firestore().collection('users').doc(uid).delete();

        res.status(200).send({ message: `User with UID ${uid} deleted successfully.` });
    } catch (error) {
        console.error(`Error deleting user: ${error}`);
        res.status(500).send({ error: error.message });
    }
});

// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
