#! /usr/bin/python

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.node import OVSController,CPULimitedHost
from mininet.link import TCLink
from mininet.cli import CLI

class SingleSwitchTopo(Topo):
	"Single switch connected to n hosts"
	def build(self,n=2):
		print('N = %s' %(n))
		switch_1 = self.addSwitch('s1')
		switch_2 = self.addSwitch('s2')
		host=self.addHost('a')
		self.addLink(host,switch_1,bw=5,delay='3ms',loss=2,max_queue_size=300)
		host=self.addHost('b')
		self.addLink(host,switch_1,bw=5,delay='3ms',loss=2,max_queue_size=300)
		host=self.addHost('e')
		self.addLink(host,switch_1,bw=5,delay='3ms',loss=2,max_queue_size=300)
		host=self.addHost('c')
		self.addLink(host,switch_2,bw=5,delay='3ms',loss=2,max_queue_size=300)
		host=self.addHost('d')
		self.addLink(host,switch_2,bw=5,delay='3ms',loss=2,max_queue_size=300)
		host=self.addHost('u')
		self.addLink(host,switch_2,bw=5,delay='3ms',loss=2,max_queue_size=300)
		self.addLink(switch_1,switch_2,bw=15,delay='2ms')

def perfTest():
	topo = SingleSwitchTopo(6)
	net = Mininet(topo=topo,host=CPULimitedHost,link=TCLink,controller=OVSController)
	net.start()
	print "Dumping the Node Information"
	dumpNodeConnections(net.hosts)
	print "Testing the network connectivity"
	net.pingAll()
	for h in net.hosts:
		print('h : %s, Ip : %s, MAC : %s' %(h,h.IP(),h.MAC()))
	#print('switch : s1 , IP : %s , MAC : %s' %(net['s1'].IP(),net['s1'].MAC()))
	#print('switch : s2 , IP : %s , MAC : %s' %(net['s2'].IP(),net['s2'].MAC()))
	#CLI(net)
	net.stop()

if __name__ == '__main__':
	#Start mininet using perfTest Function
	setLogLevel('info')
	perfTest()
	
