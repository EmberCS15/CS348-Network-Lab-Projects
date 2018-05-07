#! /usr/bin/python

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.nodelib import NAT
from mininet.log import setLogLevel
from mininet.cli import CLI
from mininet.util import irange
from mininet.util import dumpNodeConnections
from mininet.node import OVSController
import time
from threading import Thread

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


class InternetTopo(Topo):
    "Single switch connected to n hosts."
    def build(self, n=2):
        # set up inet switch
        inetSwitch = self.addSwitch('s2')
        # add inet host
        inetHost1 = self.addHost('s')
        inetHost2 = self.addHost('t')
        self.addLink(inetSwitch, inetHost1)
        self.addLink(inetSwitch, inetHost2)

        inetIntf = 'nat1-eth1'
        localIntf = 'nat1-eth2'
        localIP = '192.168.1.1'
        localSubnet = '192.168.1.0/24'
        natParams = { 'ip' : '%s/24' % localIP }
            # add NAT to topology
        nat = self.addNode('nat1', cls=NAT, subnet=localSubnet,
                               inetIntf=inetIntf, localIntf=localIntf)
        switch = self.addSwitch('s1')
            # connect NAT to inet and local switches
        self.addLink(nat, inetSwitch, intfName1=inetIntf)
        self.addLink(nat, switch, intfName1=localIntf, params1=natParams)
        host1 = self.addHost('h1',
                               ip='192.168.1.100/24',
                                defaultRoute='via %s' % localIP)
        host2 = self.addHost('h2',
                               ip='192.168.1.101/24',
                                defaultRoute='via %s' % localIP)
        host3 = self.addHost('h3',
                               ip='192.168.1.102/24',
                                defaultRoute='via %s' % localIP)

        self.addLink(host1, switch)
        self.addLink(host2, switch)
        self.addLink(host3, switch)


def run():
    "Create network and run the CLI"
    topo = InternetTopo()
    net = Mininet(topo=topo, controller =OVSController)
    net.start()
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    print "Testing network connectivity"
    net.pingAll()
    s,t,h1,h2,h3 = net.get('s'),net.get('t'),net.get('h1'),net.get('h2'),net.get('h3')
    m_thread=[None]*5
    m_thread[0]=ExecutePythonCode(s,"iperf -s -p 5566 > result-s")
    m_thread[0].start()
    m_thread[1]=ExecutePythonCode(t,"iperf -s -p 5566 > result-t")
    m_thread[1].start()
    time.sleep(3)
    print('NAT Public IP : %s' %(net['nat1'].IP()))
    for i in range(1,4):
        print('Connecting host %s( IP : %s)with servers s and t' %(i,net['h%s' %(i)].IP()))
        net['h%s' %(i)].cmd('iperf -c %s -p 5566 -t 4' %(s.IP()))
        time.sleep(2)
        net['h%s' %(i)].cmd('iperf -c %s -p 5566 -t 4' %(t.IP()))
        time.sleep(2)
        print('Finished with host %s ...............' %(i))
    print('Simulation End.........')
    print('You can see the results of the code in the files named result-s for server s, and result-t for server t')
    print('Press CTRL+SHIFT+Z to finish program. Execute sudo mn -c to clear connections')
    #net.stop()
    
if __name__ == '__main__':
    setLogLevel('info')
    run()
