## Detecting copies of yourself on a local network

The application detects copies of itself on the local network using multicast UDP messaging. The application monitors the appearance and disappearance of other copies of itself on the local network and displays a list of addresses of "live" copies upon changes.

The address of the multicast group is passed as a parameter to the application. The application supports both IPv4 and IPv6 networks, selecting the protocol automatically depending on the transmitted group address.
