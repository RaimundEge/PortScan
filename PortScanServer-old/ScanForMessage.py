#!/usr/bin/env python3
# -*- coding: utf-8 -*-
#
#  ScanForMessage.py
#  
#  Copyright 2015 Raimund Ege <ege@ubuntu>
#  
#	- find udp port, send group name, get RC4 secret key
#	- find tcp port, send group name, get message and RC4 decrypt with secret key
#  
import socket, datetime, sys
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
backend = default_backend()

HOST = "10.158.56.43"
BASE_PORT = 9000
MESSAGE = "group X"

print("Trying UDP ports: ")
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

for p in range(101):
	try:
		port = BASE_PORT + p
		print(port),
		sock.sendto(MESSAGE, (HOST, port))
		sock.settimeout(0.05)
		(keyData, (fromIP, fromPort)) = sock.recvfrom(16)
		timeStamp = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')  
		print("\nSuccess for UDP port: %d, sent: %s" % (port, MESSAGE))	
		if "Error" in keyData:
			print("%s: %s"% (timeStamp, keyData))
		else:				
			keyString = " ".join(hex(ord(n)) for n in keyData)
			print("%s: %s"% (timeStamp, keyString))
		break
	except socket.error, msg:
		sys.stdout.write(',')
		sys.stdout.flush()

if len(keyData) != 16:
	print("Key is invalid")
	sys.exit(1)
	
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

print("Trying TCP ports: ")

for p in range(101):
	try:
		port = BASE_PORT + p
		print(port),
		sock.connect((HOST, port))
		sock.send(MESSAGE)
		print("\nSuccess for TCP port: %d, sent: %s"% (port, MESSAGE))
		timeStamp = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S') 
		# prepare decryption with key
		algorithm = algorithms.ARC4(keyData)
		cipher = Cipher(algorithm, mode=None, backend=default_backend())
		decryptor = cipher.decryptor()
		# loop to get stream data
		data = sock.recv(1024)
		count = 0
		while len(data) > 0:
			count += len(data)
			# decrypt with key
			fortune = decryptor.update(data)
			sys.stdout.write(fortune)
			# check if there is more to read
			data = sock.recv(1024)
		print("%s: received %d bytes" % (timeStamp, count))
		break
	except socket.error, msg:
		sys.stdout.write(',')	
		sys.stdout.flush()
