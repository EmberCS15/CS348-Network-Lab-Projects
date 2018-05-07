import matplotlib.pyplot as plt
import numpy as np

y,x = np.loadtxt('coordinates.txt', delimiter=',', unpack=True)
plt.plot(x,y, label='Graph')

plt.xlabel('loadfactor')
plt.ylabel('avg. delay')
plt.title('Average Delay vs LoadFactor')
plt.legend()
plt.show()

y,x = np.loadtxt('dropCoordinates.txt', delimiter=',', unpack=True)
plt.plot(x,y, label='Graph')

plt.xlabel('loadfactor')
plt.ylabel('Packet Loss Rate')
plt.title('Packet Loss Rate vs LoadFactor')
plt.legend()
plt.show()