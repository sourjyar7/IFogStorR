import matplotlib.pyplot as plt

# line 1 points
x1 = [1, 2, 3, 4, 5]
y1 = [90.051, 155.955, 227.146, 302.000, 412.796]
# plotting the line 1 points
plt.plot(x1, y1, label="Nearest dh")

# line 2 points
x2 = [1, 2, 3, 4, 5]
y2 = [118.571, 206.962, 323.506, 457.327, 613.547]
# plotting the line 2 points
plt.plot(x2, y2, label="Cloud Only")

# line 3 points
x3 = [1, 2, 3, 4, 5]
y3 = [54.087, 67.602, 74.127, 87.965, 92.862]
# plotting the line 3 points
plt.plot(x3, y3, label="IFogStor")

# line 3 points
x4 = [1, 2, 3, 4, 5]
y4 = [54.121, 59.039, 65.636, 72.014, 82.197]
# plotting the line 3 points
plt.plot(x4, y4, label="IFogStorR(4)")

# line 3 points
x5 = [1, 2, 3, 4, 5]
y5 = [211.522, 281.012, 345.799, 454.941, 534.360]
# plotting the line 3 points
plt.plot(x5, y5, label="IFogStorR(8)")


# naming the x axis
plt.xlabel('Consumers per Data Item(cp)')
# naming the y axis
plt.ylabel('Overall Latency')
# giving a title to my graph
plt.title('Overall Latency vs cp')

# show a legend on the plot
plt.legend()

# function to show the plot
plt.show()
