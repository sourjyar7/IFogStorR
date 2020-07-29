import matplotlib.pyplot as plt

x1 = [56, 112, 168]
y1 = [101.8, 148, 434.2]
# plotting the line 3 points
plt.plot(x1, y1, label="IFogStor")

x2 = [56, 112, 168]
y2 = [34.8, 90.4, 368.8]
# plotting the line 3 points
plt.plot(x2, y2, label="IFogStorR")


# naming the x axis
plt.xlabel('No.of Gateways')
# naming the y axis
plt.ylabel('Avg. Solving Time')
# giving a title to my graph
plt.title('Solving Time vs No.of Gateways')

# show a legend on the plot
plt.legend()

# function to show the plot
plt.show()
