## File transfer over TCP with data transfer rate calculation
A protocol for transferring an arbitrary file from one computer to another has been developed, and a client and server implementing this protocol have been written. The server also outputs the data reception rate from the client.

The server is given the port number in the parameters where it will wait for incoming connections from clients.

The relative or absolute path to the file to be sent is passed to the client in the parameters. The file name does not exceed 4096 bytes in UTF-8 encoding. The file size is no more than 1 terabyte. The DNS name (or IP address) and the server port number are also transmitted to the client in the parameters.

The server saves the received file to the uploads subdirectory of its current directory. The file name, if possible, matches the name that the client sent. The server never writes outside of the uploads directory.
In the process of receiving data from the client, the server outputs the instantaneous reception rate and the average speed per session to the console every 3 seconds. The speeds are displayed separately for each active client. If the client has been active for less than 3 seconds, the speed should still be output for him once. Speed here refers to the number of bytes transmitted per unit of time.
After successfully saving the entire file, the server checks whether the size of the received data matches the size transmitted by the client, and informs the client about the success or failure of the operation, after which it closes the connection.
The client displays a message indicating whether the file transfer was successful.
