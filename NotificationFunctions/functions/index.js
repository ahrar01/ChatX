'use strict'

const functions = require('firebase-functions');
const admin =  require('firebase-admin');

admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((data,context) => {

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification from : ', user_id);

  /*
   * Stops proceeding to the rest of the function if the entry is deleted from database.
   * If you want to work with what should happen when an entry is deleted, you can replace the
   * line from "return console.log.... "
   */


  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

  const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;

    console.log('You have new notification from  : ', from_user_id);

    /*
     * The we run two queries at a time using Firebase 'Promise'.
     * One to get the name of the user who sent the notification
     * another one to get the devicetoken to the device we want to send notification to
     */

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : "New Friend Request",
          body: `${userName} has sent you request`,
          icon: "default",
          click_action : "com.appswedevelop.mychat_TARGET_NOTIFICATION"
        },
        data : {
          from_user_id : from_user_id
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was the notification Feature');

      });

    });

  });

});


/*
'use strict'

const functions = require('firebase-functions');
const admin =  require('firebase-admin');

admin.initializeApp(functions.config().firebase);
exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((data,context) => {

const user_id = context.params.user_id;
const notification = context.params.notification;
console.log('A new notification from:', user_id);
return true;

});
*/
