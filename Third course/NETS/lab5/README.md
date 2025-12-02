## SOCKS5 proxy
1. The implementation is a proxy server conforming to the SOCKS version 5 standard;
2. In the parameters, the program is passed only the port on which the proxy will wait for incoming connections from clients;
3. Of the three commands available in the protocol, only command 1 (establish a TCP/IP stream connection) is implemented Authentication and IPv6 addresses are not supported;
4. Non-blocking sockets within a single thread are used to implement proxies. Additional threads are not used. That is, there are no blocking calls (except for the selector call);
5. The proxy makes no assumptions about which application layer protocol will be used inside the forwarded TCP connection. In particular, data transfer is supported simultaneously in both directions, and connections are closed carefully (only after they are no longer needed);
6. There are no idle cycles in the application in any situations. In other words, there is no program state in which the loop body is repeatedly executed, which does not do any actual data transfer per iteration;
7. 130 KB is allocated per client;
8. The proxy supports domain name resolution (value 0x03 in the address field). Resolving is also non-blocking. The following approach is used for this:
  - At the start of the program, a new UDP socket is created, which is added to the read selector;
  - When a domain name needs to be resolved, an A-record DNS query is sent through this socket to the address of the recursive DNS resolver;
  - The socket reader handles the case when a response to a DNS query is received, and then work continues with the received address;
  - The `dnsjava` library is used to obtain the address of a recursive resolver, as well as to generate and parse DNS messages in Java;

## Running
To compile:
`javac -cp .;dnsjava-3.6.3.jar;slf4j-api-2.0.12.jar Main.java`
To run:
`java -cp .;dnsjava-3.6.3.jar;slf4j-api-2.0.12.jar Main <port>`
