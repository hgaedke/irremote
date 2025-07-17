# irremote
Android app: remote control for internet radio

This app can be used on an Android 6 (or newer) device to remote-control the internet radio app which is available here: https://github.com/hgaedke/iradio_angular

The IP address of the internet radio is currently hard-coded in MainActivity.kt. For a local test, don't forget to change the IP to 10.0.2.2 (see commented-out code).

The websocket implementation refers to the communication protocol described on https://github.com/hgaedke/iradio_angular