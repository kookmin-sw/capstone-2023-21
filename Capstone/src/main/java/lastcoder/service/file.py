import sys

def binary(input_str):
    return input_str[0:5]

if __name__ == '__main__':
    input_str = sys.argv[1]
    output_str = binary(input_str)
    print(output_str)