import matplotlib.pyplot as plt
import numpy as np

#1
x,y = np.loadtxt('AverageDelayVsLamda.txt', delimiter=',', unpack=True)
plt.plot(x,y, label='Graph')

plt.xlabel('lambda')
plt.ylabel('avg. delay')
plt.title('Average Delay vs Lambda')
plt.legend()
plt.show()

#2
x,y = np.loadtxt('AverageQueueVSLamda.txt', delimiter=',', unpack=True)
plt.plot(x,y, label='Graph')

plt.xlabel('lambda')
plt.ylabel('avg. queue size')
plt.title('Average Queue Size vs Lambda')
plt.legend()
plt.show()

#3
data=[]
d2=[]
val=0
x,y = np.loadtxt('AverageDelayPerSource.txt',delimiter=',',unpack=True)
for i in range(0,len(x)):
	if x[i]!=val:
		val=x[i]
		data.append(d2)
		d2=[]
	d2.append(y[i])

data.append(d2)
plt.figure()
plt.boxplot(data)
plt.xlabel('Source')
plt.ylabel('Avg. Packet Delay')
plt.title('Average Packet Delay per Source')
plt.legend()
plt.show()

#4
data=[]
d2=[]
val=0
x,y = np.loadtxt('AveragePacketDropPerSource.txt',delimiter=',',unpack=True)
for i in range(0,len(x)):
	if x[i]!=val:
		val=x[i]
		data.append(d2)
		d2=[]
	d2.append(y[i])

data.append(d2)
plt.figure()
plt.boxplot(data)
plt.xlabel('Source')
plt.ylabel('Avg. Packet Drop')
plt.title('Average Packet Drop per Source')
plt.legend()
plt.show()

#5
x,y = np.loadtxt('PoissonDistribution.txt', delimiter=',', unpack=True)
plt.plot(x,y, label='Graph')

plt.xlabel('Num of Packets')
plt.ylabel('Num. of Time Intervals')
plt.title('Poisson Graph (Lambda Per Source = 50.15, Number of Sources = 4)')
plt.legend()
plt.show()


