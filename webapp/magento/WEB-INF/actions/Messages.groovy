/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
eventNotificationMessageList = [];
if (session.getAttribute("_EVENT_MESSAGE_")) {
    eventMessageList.add(session.getAttribute("_EVENT_MESSAGE_"))
    session.removeAttribute("_EVENT_MESSAGE_");
}
context.eventNotificationMessageList = eventMessageList;
context.eventMessageList = [];
if (eventNotificationMessageList) {
    request.removeAttribute("eventMessageList");
    request.removeAttribute("_EVENT_MESSAGE_");
    request.removeAttribute("_EVENT_MESSAGE_LIST_");
}

errorNotificationMessageList = [];
if (session.getAttribute("_ERROR_MESSAGE_")) {
    errorNotificationMessageList.add(session.getAttribute("_ERROR_MESSAGE_"))
    session.removeAttribute("_ERROR_MESSAGE_");
}
context.errorNotificationMessageList = errorMessageList;
context.errorMessageList = [];
if (errorNotificationMessageList) {
    request.removeAttribute("errorMessageList");
    request.removeAttribute("_ERROR_MESSAGE_");
    request.removeAttribute("_ERROR_MESSAGE_LIST");
}
