#! /usr/bin/python

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.node import OVSController
from mininet.cli import CLI
from threading import Thread
import time,sys

class ExecutePythonCode(Thread):
	def __init__(self,nd,fname):
		Thread.__init__(self)
		self.nd=nd
		self.fname=fname
	def run(self):
		"Executing Code ........ "
		s=self.fname
		result=self.nd.cmd(s)
		print "Thread Executed %s" %(s)
		#print "Result Received : %s" %(result)

class SingleSwitchTopo(Topo):
	"Single switch and 2 hosts"
	def build(self,n=2):
		switch=self.addSwitch('s1')
		for h in range(n):
			host = self.addHost('h%s' %(h+1))
			self.addLink(host,switch)

def createTopo():
	topo = SingleSwitchTopo(2)
	net = Mininet(topo=topo,controller=OVSController)
	net.start()
	print "Dumping the Node Information.............."
	dumpNodeConnections(net.hosts)
	print "Testing the network connectivity.........."
	net.pingAll()
	#The code goes here
	h1,h2=net['h1'],net['h2']
	print "Executing Server Commands....."
	m_server = ExecutePythonCode(h1,'iperf -s -p 5566 -i 2 > resultCode')
	m_server.start()
	time.sleep(3)
	print "Executing Client Commands....."
	m_client = ExecutePythonCode(h2,'iperf -c %s -p 5566 -t 20' %(h1.IP()))
	m_client.start()	
	time.sleep(23)
	#CLI(net)
	print "Stopping the network.............."
	print "The results have been directed the file named resultCode in the same directory as the code. "
	print "Press CTRL+SHIFT+Z to exit. Execute sudo mn -c to clear active connections"
	#net.stop()
	sys.exit(0)

if __name__=='__main__':
	setLogLevel('info')
	createTopo()
