import serial
import matplotlib.pyplot as plt
import numpy as np
from sets import Set

ser = serial.Serial('COM10', 9600)
print "Established connection. Try to read some data off"

print "Reading the Empty Spectrum - For Ambient Light Cancellation"
raw_data = ""
while ser.isOpen():
    raw_data = ser.readline()
    if raw_data.find("W") == -1:
        break

raw_data = raw_data.strip()
raw_data = raw_data[0:-1]
data = [i for i in raw_data.split(",")]
integer_data = [int(unicode(stringval, errors='ignore')) for stringval in data]
ambient_light = integer_data
plt.plot(integer_data)
plt.title('Ambient Light Spectrum')
plt.savefig("AmbientLight.png")
plt.close()

print "Reading the Spectrum with Laser On - No Filter"

raw_data = ""
while ser.isOpen():
    raw_data = ser.readline()
    if raw_data.find("W") == -1:
        break
raw_data = raw_data.strip()
raw_data = raw_data[0:-1]
data = [i for i in raw_data.split(",")]
integer_data = [int(unicode(stringval, errors='ignore')) for stringval in data]
integer_data = [a_i - b_i for a_i, b_i in zip(integer_data, ambient_light)]
plt.plot(integer_data)
plt.title('No Filter Spectrum')
plt.savefig("NoFilterSpectrum.png")
plt.close()

print "Reading the Spectrum with Low Pass Edge"

raw_data = ""
while ser.isOpen():
    raw_data = ser.readline()
    if raw_data.find("W") == -1:
        break

raw_data = raw_data.strip()
raw_data = raw_data[0:-1]
data = [i for i in raw_data.split(",")]
integer_data = [int(unicode(stringval, errors='ignore')) for stringval in data]
integer_data = [a_i - b_i for a_i, b_i in zip(integer_data, ambient_light)]
left_spectrum = integer_data
plt.plot(integer_data)
plt.title('Low Pass Spectrum')
plt.savefig("LowPassSpectrum.png")
plt.close()

print "Reading the Spectrum with High Pass Edge"

while ser.isOpen():
    raw_data = ser.readline()
    if raw_data.find("W") == -1:
        break

raw_data = raw_data.strip()
raw_data = raw_data[0:-1]
data = [i for i in raw_data.split(",")]
integer_data = [int(unicode(stringval, errors='ignore')) for stringval in data]
integer_data = [a_i - b_i for a_i, b_i in zip(integer_data, ambient_light)]
right_spectrum = integer_data
plt.plot(integer_data)
plt.title('High Pass Filter Spectrum')
plt.savefig("HighPassSpectrum.png")
plt.close()

integer_data = [a_i + b_i for a_i, b_i in zip(left_spectrum, right_spectrum)]
plt.plot(integer_data)
plt.title('High Pass Filter Spectrum')
plt.savefig("CombinedSpectrum.png")
plt.close()

with open("test.txt","r") as myfile:
    data = myfile.read()
    last_data = data.split("\n")[-2]

last_data = last_data.split(",")
last_data = [int(val) for val in last_data]
count = []

unique_count = Set()

for i in range(0,len(last_data)):
    if integer_data[i] < last_data[i]:
        value = float(integer_data[i])/float(last_data[i])
    else:
        value = float(last_data[i])/float(integer_data[i])
    if not value in unique_count:
        unique_count.add(value)
    count.append(value)

unique_count_list = list(unique_count)
unique_count_list.sort()

for i in range(0,len(unique_count_list)):
    print str(unique_count_list[i]) + " ->" + str(count.count(unique_count_list[i]))


with open("test.txt", "a") as myfile:
    myfile.write(",".join([str(val) for val in integer_data]))
    myfile.write("\n")
