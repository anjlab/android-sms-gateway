Android SMS Gateway Application
===============

This is a simple and small application that translates Google Cloud Messaging (GCM) events into outgoing SMS messages on your phone.

Getting Started
===============
* Follow the instructions gescribed at http://developer.android.com/google/gcm/gs.html to create your GCM Sender ID (Project ID) and API Key.
* Build application from the source code using Eclipse or download it from the Releases section of this project.
* Install it and launch on your phone. Put in your Sender ID and save a Registration ID so you can use it from your web app (or something).
* Set up you backend application as it described below

Setting up your backend application 
===============

To send messages you will need to do a HTTP POST to https://android.googleapis.com/gcm/send as it described here:
http://developer.android.com/google/gcm/http.html

Make sure you send API key from your Google Console within `Authorization` header and the `registration_id` value saved from your phone withing post body. 

Here is a small sample witten in ruby:
```ruby
require 'mechanize'

@agent = Mechanize.new

@agent.post 'https://android.googleapis.com/gcm/send',
                          {
                              'registration_id' => 'YOUR REGISTRATION ID VALUE',
                              'data.message' => 'YOUR MESSAGE HERE',
                              'data.number' => 'DESTINATION PHONE NUMBER HERE'
                          },
                          {
                              'Authorization' => 'key=YOUR API KEY HERE'
                          }
```
**That's it! Make sure you have enough credits on your phone to send text messages :)**
