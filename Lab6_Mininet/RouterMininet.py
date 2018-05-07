#!/usr/bin/python
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.nodelib import NAT
from mininet.log import setLogLevel
from mininet.cli import CLI
from mininet.util import dumpNodeConnections
from mininet.node import OVSController
from mininet.util import irange
from mininet.node import Node
import mininet.node


class LinuxRouter( Node ):
    "A Node with IP forwarding enabled."

    def config( self, **params ):
        super( LinuxRouter, self).config( **params )
        # Enable forwarding on the router
        self.cmd( 'sysctl net.ipv4.ip_forward=1' )

    def terminate( self ):
        self.cmd( 'sysctl net.ipv4.ip_forward=0' )
        super( LinuxRouter, self ).terminate()

class routerTopo(Topo):
    def build( self, **_opts ):
        routerIP=['10.0.1.1/24','10.0.2.1/24','10.0.3.1/24','10.0.4.1/24']
        hostIP=['10.0.1.2/24','10.0.2.2/24']
        R1=self.addNode('R1',cls=LinuxRouter,ip=routerIP[0],defaultRoute='via 11.0.1.2')
        R2=self.addNode('R2',cls=LinuxRouter,ip=routerIP[1],defaultRoute='via 11.0.4.2')
        R3=self.addNode('R3',cls=LinuxRouter,ip=routerIP[2],defaultRoute='via 11.0.3.1')
        R4=self.addNode('R4',cls=LinuxRouter,ip=routerIP[3],defaultRoute='via 11.0.2.1')
        s1=self.addSwitch('s1')
        s2=self.addSwitch('s2')
        H1=self.addHost('H1',ip=hostIP[0],defaultRoute='via 10.0.1.1')
        H7=self.addHost('H7',ip=hostIP[1],defaultRoute='via 10.0.2.1')
        H2 = self.addHost('H2',ip='10.0.3.2',defaultRoute='via 10.0.3.1')
        H3 = self.addHost('H3',ip='10.0.3.3',defaultRoute='via 10.0.3.1')
        H4 = self.addHost('H4',ip='10.0.3.4',defaultRoute='via 10.0.3.1')
        H5 = self.addHost('H5',ip='10.0.4.5',defaultRoute='via 10.0.4.1')
        H6 = self.addHost('H6',ip='10.0.4.6',defaultRoute='via 10.0.4.1')
        self.addLink(H2,s1)
        self.addLink(H3,s1)
        self.addLink(H4,s1)
        self.addLink(H5,s2)
        self.addLink(H6,s2)
        #Host to router connections
        self.addLink(s1,R3,intfName2='R3-eth0',params2={'ip':'10.0.3.1/24'})
        self.addLink(s2,R4,intfName2='R4-eth0',params2={'ip':'10.0.4.1/24'})
        self.addLink(H1,R1,intfName2='R1-eth0',params2={'ip':'10.0.1.1/24'})
        self.addLink(H7,R2,intfName2='R2-eth0',params2={'ip':'10.0.2.1/24'})

        #Router to Router connections
        self.addLink(R1,R2,intfName1='R1-eth1',intfName2='R2-eth1',params1={'ip':'11.0.1.1/24'},params2={'ip':'11.0.1.2/24'})
        self.addLink(R3,R4,intfName1='R3-eth1',intfName2='R4-eth1',params1={'ip':'11.0.2.1/24'},params2={'ip':'11.0.2.2/24'})
        self.addLink(R1,R3,intfName1='R1-eth2',intfName2='R3-eth2',params1={'ip':'11.0.3.1/24'},params2={'ip':'11.0.3.2/24'})
        self.addLink(R2,R4,intfName1='R2-eth2',intfName2='R4-eth2',params1={'ip':'11.0.4.1/24'},params2={'ip':'11.0.4.2/24'})

def configureHostR3(host):
    host.cmd('route add -net 10.0.2.0 netmask 255.255.255.0 gw 10.0.3.1')
    host.cmd('route add -net 10.0.4.0 netmask 255.255.255.0 gw 10.0.3.1')
    host.cmd('route add -net 10.0.1.0 netmask 255.255.255.0 gw 10.0.3.1')

def configureHostR4(host):
    host.cmd('route add -net 10.0.2.0 netmask 255.255.255.0 gw 10.0.4.1')
    host.cmd('route add -net 10.0.1.0 netmask 255.255.255.0 gw 10.0.4.1')
    host.cmd('route add -net 10.0.3.0 netmask 255.255.255.0 gw 10.0.4.1')

def configureRouter(rno,router):
    with open("R{}_iptable.txt".format(rno)) as f:
        lines = f.readlines()
    lines = [x.strip() for x in lines]
    for i in range(1,len(lines)):
        routeInfo = lines[i]
        routeInfo = routeInfo.replace('\t'," ")
        routeInfo = routeInfo.split()
        #print(routeInfo)
        router.cmd('route add -net {} netmask {} gw {}'.format(routeInfo[0],routeInfo[2],routeInfo[1]))

def createTopo():
    topo = routerTopo()
    net = Mininet(topo=topo,controller=OVSController)
    net.start()
    for i in range(2,7):
        if(i<5):
            configureHostR3(net.get('H%i'%i))
        else:
            configureHostR4(net.get('H%i'%i))
    #configure Router
    for i in range(1,5):
        print("configuring Router %i......."%i)
        configureRouter(i,net.get('R%i'%i))
    #Ping all nodes from all other nodes
    net.pingAll()
    print('use cmd H1 traceroute H3 to trace the route from H1 to H3')
    #open mininet CLI and use cmd H1 traceroute H3 to trace the route from H1 to H3
    #to install traceroute open ubuntu terminal and sudo apt-get install traceroute
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    createTopo()

