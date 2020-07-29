import matplotlib.pyplot as plt


# x-coordinates of left sides of bars
left = [4, 8, 12]

# heights of bars
height = [34.8, 90.4, 368.8]

# labels for bars
tick_label = ['iFogStor(4)', 'iFogStor(8)', 'iFogStor(12)']

# plotting a bar chart
plt.bar(left, height, tick_label=tick_label,
        width=0.8, color=['red', 'green', 'orange'])

# naming the x-axis
plt.xlabel('Replication Factor')
# naming the y-axis
plt.ylabel('Avg. Solution Time')
# plot title
plt.title('Average Solution Time vs Replication Factor')


# function to show the plot
plt.show()
