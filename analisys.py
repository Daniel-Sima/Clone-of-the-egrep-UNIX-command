import numpy as np 
import matplotlib.pyplot as plt 

regex = []
timeGrep = []
timeReg = []

f = open('sample.txt','r') 
for row in f: 
    row = row.split(' ') 
    regex.append(row[0]) 
    timeReg.append(int(row[1]))
    timeGrep.append(int(row[2])) 

X_axis = np.arange(len(regex)) 

plt.bar(X_axis - 0.2, timeReg, 0.4, label = 'RegEx') 
plt.bar(X_axis + 0.2, timeGrep, 0.4, label = 'grep') 

plt.xticks(X_axis, regex) 
plt.xlabel("RegEx") 
plt.ylabel("Time (ms)") 
plt.title("Execution time comparison") 
plt.legend() 
plt.show() 
