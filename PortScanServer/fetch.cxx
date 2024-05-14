/*
 * fetch.cxx
 * 
 * 	reference implementation for Assignment 5
 * 
 * 	scans upd ports 9000 - 9100 on blitz.cs.niu.edu
 *  gets 128bit key
 *  scans TCP ports 9000 - 9100 on blitz.cs.niu.edu
 *  gets encrypted message
 *  decrypys message with RC4
 * 	      
 */
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <netinet/in.h>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/err.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iostream>

using namespace std;
#define MAX 4096
#define IP "100.72.194.4"
#define TIMEOUT 400000

void handleErrors() {
  ERR_print_errors_fp(stderr);
  abort();
}

bool scanPortUdp(int port, unsigned char* key) {
	// Create the UDP socket
	int sock, rc;
	unsigned int addrlen;
	
	if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
	{
		perror("Failed to create socket");
		exit(EXIT_FAILURE);
	}
	
	struct timeval read_timeout;
	read_timeout.tv_sec = 0;
	read_timeout.tv_usec = TIMEOUT;
	setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &read_timeout, sizeof read_timeout);

	struct sockaddr_in echoserver; // structure for address of server	
	// Construct the server sockaddr_in structure
	memset(&echoserver, 0, sizeof(echoserver)); /* Clear struct */
	echoserver.sin_family = AF_INET;			/* Internet/IP */
	echoserver.sin_addr.s_addr = inet_addr(IP); /* IP address */
	echoserver.sin_port = htons(port); /* server port */

	// Send the message to the server
	rc = sendto(sock, "group Y", 7, 0, (struct sockaddr *)&echoserver, sizeof(echoserver));
	if (rc == -1)
	{
		perror("sendto");
		exit(EXIT_FAILURE);
	}

	// Receive the message back from the server
	addrlen = sizeof(echoserver);
	rc = recvfrom(sock, key, 16, 0, (struct sockaddr *)&echoserver, &addrlen);
	if (rc == -1)
	{
		cout << "." << flush;
		return false;
	}
	cout << " Server (" << inet_ntoa(echoserver.sin_addr) << ":" << port << ") sent: " << rc << " bytes\n";
	close(sock);
	return true;
}

bool scanPortTcp(int port, unsigned char* buffer, int* buffer_len) {
	int received = 0;
	
	int sock;
	struct sockaddr_in echoserver;  // structure for address of server
	
	// Create the TCP socket
	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		perror("Failed to create socket");
		exit(EXIT_FAILURE);
	}

	struct timeval read_timeout;
	read_timeout.tv_sec = 0;
	read_timeout.tv_usec = TIMEOUT;
	setsockopt(sock, SOL_SOCKET, SO_SNDTIMEO, &read_timeout, sizeof read_timeout);

	// Construct the server sockaddr_in structure
	memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
	echoserver.sin_family = AF_INET;                   /* Internet/IP */
	echoserver.sin_addr.s_addr = inet_addr(IP);   /* IP address */
	echoserver.sin_port = htons(port);        /* server port */
	
	// connect to server
	if (connect(sock, (struct sockaddr *) &echoserver, sizeof(echoserver)) < 0) {
		cout << "." << flush;
		return false;
	}
	
	// Send the message to the server 
	if (write(sock, "group Y", 7) == -1) {
		perror("write");
		exit(EXIT_FAILURE);
	}
	// Receive the message back from the server 
	if ((received = read(sock, buffer, *buffer_len)) == -1) {
		perror("read");
		exit(EXIT_FAILURE);
	}
	
	*buffer_len = received;
	cout << " Server (" << inet_ntoa(echoserver.sin_addr) << ":" << port << ") sent: " << received << " bytes\n";
	
	close(sock);
	return true;
}

void decrypt(unsigned char *ciphertext, int ciphertext_len, unsigned char *key) {

  EVP_CIPHER_CTX *ctx;
  int len;
  unsigned char plaintext[MAX];
  int plaintext_len;

  // Create and initialise the context 
  if(!(ctx = EVP_CIPHER_CTX_new())) handleErrors();

  // Initialise the decryption operation.
  if(1 != EVP_DecryptInit_ex(ctx, EVP_rc4(), NULL, key, 0)) handleErrors();

  // Provide the message to be decrypted, and obtain the plaintext output.
  if(1 != EVP_DecryptUpdate(ctx, plaintext, &len, ciphertext, ciphertext_len)) handleErrors();
  plaintext_len = len;

  // Finalise the decryption. Further plaintext bytes may be written at this stage.
  if(1 != EVP_DecryptFinal_ex(ctx, plaintext + len, &len)) handleErrors();
  plaintext_len += len;

  /* Clean up */
  EVP_CIPHER_CTX_free(ctx);

  plaintext[plaintext_len] = '\0';
  cout << plaintext << endl;
}

void scanPortsTcp(unsigned char* key) {
	cout << "scanning TCP ports : ";
	for (int port = 9000; port <= 9100; port++)
	{		
		unsigned char buffer[MAX];
		int buffer_len = MAX;
		if (scanPortTcp(port, buffer, &buffer_len)) {
			decrypt( buffer, buffer_len, key);
			break;
		}
	}	
}

int main()
{
	cout << "trying UDP ports: ";
	for (int port = 9000; port <= 9100; port++)
	{		
		unsigned char key[16];
		if (scanPortUdp(port, key)) {
			scanPortsTcp(key);
			break;
		}
	}	
	return 0;
}
