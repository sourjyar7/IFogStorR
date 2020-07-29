import matplotlib.pyplot as plt


# line 3 points
x3 = [1, 2, 3, 4, 5]
y3 = [100, 114, 109, 100, 90]
# plotting the line 3 points
plt.plot(x3, y3, label="IFogStor")

# line 3 points
x4 = [1, 2, 3, 4, 5]
y4 = [38, 35, 40, 62, 31]
# plotting the line 3 points
plt.plot(x4, y4, label="IFogStorR(4)")

# line 3 points
x5 = [1, 2, 3, 4, 5]
y5 = [89, 80, 87, 94, 108]
# plotting the line 3 points
plt.plot(x5, y5, label="IFogStorR(8)")

# line 3 points
x5 = [1, 2, 3, 4, 5]
y5 = [384, 460, 334, 330, 336]
# plotting the line 3 points
plt.plot(x5, y5, label="IFogStorR(8)")

# naming the x axis
plt.xlabel('Consumers per Data Item(cp)')
# naming the y axis
plt.ylabel('Solving Time')
# giving a title to my graph
plt.title('Solving Time vs cp')

# show a legend on the plot
plt.legend()

# function to show the plot
plt.show()
