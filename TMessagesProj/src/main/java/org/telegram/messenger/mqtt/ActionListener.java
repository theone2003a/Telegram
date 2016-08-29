/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.telegram.messenger.mqtt;


import org.telegram.messenger.R;
import org.telegram.messenger.mqtt.Connection.ConnectionStatus;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.telegram.messenger.shamChat.NewGroupMessageSentFailedEvent;
import org.telegram.messenger.shamChat.NewGroupMessageSentSuccessEvent;

import android.content.Context;
import android.widget.Toast;

import de.greenrobot.event.EventBus;



public class ActionListener implements IMqttActionListener {

  /**
   * Actions that can be performed Asynchronously <strong>and</strong> associated with a
   * {@link ActionListener} object
   * 
   */
  public enum Action {
    /** Connect Action **/
    CONNECT,
    /** Disconnect Action **/
    DISCONNECT,
    /** Subscribe Action **/
    SUBSCRIBE,
    /** Publish Action **/
    PUBLISH
  }

  /**
   * The {@link Action} that is associated with this instance of
   * <code>ActionListener</code>
   **/
  private Action action;
  /** The arguments passed to be used for formatting strings**/
  private String[] additionalArgs;
  /** Handle of the {@link Connection} this action was being executed on **/
  private String clientHandle;
  /** {@link Context} for performing various operations **/
  private Context context;
  
  private boolean DEBUG = false;
  /**
   * Creates a generic action listener for actions performed from any activity
   * 
   * @param context
   *            The application context
   * @param action
   *            The action that is being performed
   * @param clientHandle
   *            The handle for the client which the action is being performed
   *            on
   * @param additionalArgs
   *            Used for as arguments for string formating
   */
  public ActionListener(Context context, Action action,
      String clientHandle, String... additionalArgs) {
    this.context = context;
    this.action = action;
    this.clientHandle = clientHandle;
    this.additionalArgs = additionalArgs;
  }

  /**
   * The action associated with this listener has been successful.
   * 
   * @param asyncActionToken
   *            This argument is not used
   */
  @Override
  public void onSuccess(IMqttToken asyncActionToken) {
    switch (action) {
      case CONNECT :
        connect();
        break;
      case DISCONNECT :
        disconnect();
        break;
      case SUBSCRIBE :
        subscribe();
        break;
      case PUBLISH :
        publish();
        break;
    }

  }

  /**
   * A publish action has been successfully completed, update connection
   * object associated with the client this action belongs to, then notify the
   * user of success
   */
  private void publish() {

    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    String actionTaken = context.getString(R.string.toast_pub_success,
        (Object[]) additionalArgs);
    c.addAction(actionTaken);
    //mast - disabled

    //Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
    
    //Let other activities know that this message has been sent successfully 
    NewGroupMessageSentSuccessEvent newGroupMessageSentSuccessEvent = new NewGroupMessageSentSuccessEvent(additionalArgs[0]);
    EventBus.getDefault().postSticky(newGroupMessageSentSuccessEvent);
    
    
  }

  /**
   * A subscribe action has been successfully completed, update the connection
   * object associated with the client this action belongs to and then notify
   * the user of success
   */
  private void subscribe() {
    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    String actionTaken = context.getString(R.string.toast_sub_success,
        (Object[]) additionalArgs);
    c.addAction(actionTaken);
    
    if (DEBUG)
      NotifySimple.toast(context, actionTaken, Toast.LENGTH_SHORT);
    else
      NotifySimple.toast(context, "success", Toast.LENGTH_SHORT);
  }

  /**
   * A disconnection action has been successfully completed, update the
   * connection object associated with the client this action belongs to and
   * then notify the user of success.
   */
  private void disconnect() {
    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
    String actionTaken = context.getString(R.string.toast_disconnected);
    c.addAction(actionTaken);
    
    if (DEBUG)
      NotifySimple.toast(context, "actionListener: server "+ actionTaken, Toast.LENGTH_SHORT);

  }

  /**
   * A connection action has been successfully completed, update the
   * connection object associated with the client this action belongs to and
   * then notify the user of success.
   */
  private void connect() {

    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    c.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);
    c.addAction("Client Connected");
    
    if (DEBUG)
      NotifySimple.toast(context, "connected to group server", Toast.LENGTH_SHORT);
 
  }

  /**
   * The action associated with the object was a failure
   * 
   * @param token
   *            This argument is not used
   * @param exception
   *            The exception which indicates why the action failed
   */
  @Override
  public void onFailure(IMqttToken token, Throwable exception) {
    switch (action) {
      case CONNECT :
        connect(exception);
        break;
      case DISCONNECT :
        disconnect(exception);
        break;
      case SUBSCRIBE :
        subscribe(exception);
        break;
      case PUBLISH :
        publish(exception);
        break;
    }

  }

  /**
   * A publish action was unsuccessful, notify user and update client history
   * 
   * @param exception
   *            This argument is not used
   */
  private void publish(Throwable exception) {
    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    String action = context.getString(R.string.toast_pub_failed,
        (Object[]) additionalArgs);
    c.addAction(action);
    //mast - disabled
    //Notify.toast(context, action, Toast.LENGTH_SHORT);
   
    //Let other activities know that this message has failed 
    NewGroupMessageSentFailedEvent newMessageSentFailedEvent = new NewGroupMessageSentFailedEvent(additionalArgs[0]);
    EventBus.getDefault().postSticky(newMessageSentFailedEvent);  
    
  }

  /**
   * A subscribe action was unsuccessful, notify user and update client history
   * @param exception This argument is not used
   */
  private void subscribe(Throwable exception) {
    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    String action = context.getString(R.string.toast_sub_failed,
        (Object[]) additionalArgs);
    c.addAction(action);

    NotifySimple.toast(context, action, Toast.LENGTH_SHORT);

  }

  /**
   * A disconnect action was unsuccessful, notify user and update client history
   * @param exception This argument is not used
   */
  private void disconnect(Throwable exception) {
    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
    c.addAction("Disconnect Failed - an error occured");

  }

  /**
   * A connect action was unsuccessful, notify the user and update client history
   * @param exception This argument is not used
   */
  private void connect(Throwable exception) {
    Connection c = Connections.getInstance(context).getConnection(clientHandle);
    c.changeConnectionStatus(Connection.ConnectionStatus.ERROR);
    c.addAction("Client failed to connect");

  }



}