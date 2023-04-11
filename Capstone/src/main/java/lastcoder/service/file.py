import sys

def binary(input_str):
    hex_list = input_str.split(" ")
    result_binary = ""
    for hex in hex_list:
        result_binary += bin(int(hex, 16))[2:0].zfill(8)
    return input_str[0:5]

if __name__ == '__main__':
    input_str = sys.argv[1]
    output_str = binary(input_str)
    print(output_str)