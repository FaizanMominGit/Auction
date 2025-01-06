const functions = require("firebase-functions");

// Simple HTTP function to test setup
exports.helloWorld = functions.https.onRequest((req, res) => {
  res.json({data: "Hello, World!"});
});
