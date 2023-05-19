import os
import numpy as np


def process_files(input_file_path, output_file_path):
    # 폴더 내의 파일 목록을 가져옴
    files_to_process = os.listdir(input_file_path)
    for file_name in files_to_process:
        with open(os.path.join(input_file_path, file_name), 'rb') as f:
            data = f.read()

        # 데이터에서 유효한 16진수 문자열만 추출하여 리스트로 저장
        hex_data = []
        for line in data.decode('utf-8').split('\n'):
            hex_line = line.strip().split()[1:]
            for hex_str in hex_line:
                try:
                    if '?' in hex_str:
                        hex_str = hex_str.replace(
                            '?', '0')  # '?'가 있는 부분을 0으로 바꿔줌
                    int(hex_str, 16)
                    hex_data.append(hex_str)
                except ValueError:
                    pass
            # 파일 저장
        output_file_name = os.path.join(output_file_path, file_name)
        with open(output_file_name, 'w') as f:
            f.write('\n'.join(hex_data))


def change_extension(output_file_path):
    # 기존 확장자와 새로운 확장자 지정
    old_ext = '.bytes'
    new_ext = '.txt'

    # 각 폴더를 돌며 기존 확장자로 끝나는 파일들의 이름을 변경
    for file_path in glob.glob(output_file_path + '/*' + old_ext):
        os.rename(file_path, os.path.splitext(file_path)[0] + new_ext)


def change_to_array(output_file_path):
    file_list = []
    for root, dirs, files in os.walk(output_file_path):
        for file in files:
            file_list.append(os.path.join(root, file))  # 파일 경로 출력

    npy_list = []
    for file_path in file_list:
        hex_data = []
        len_check = False
        with open(file_path, "r") as f:
            hex_data = f.read().splitlines()

        if len(hex_data) < 128*128:
            width = height = 128
        elif len(hex_data) < 256*256:
            width = height = 256
        elif len(hex_data) < 512*512:
            width = height = 512
        else:
            width = height = 1024

        img_data = np.zeros((height, width), dtype=np.uint16)

        for i in range(height):
            for j in range(width):
                index = (i * width + j) * 2
                if index+2 >= len(hex_data):
                    len_check = True
                    break
                value1 = int(hex_data[index], 16)
                value2 = int(hex_data[index + 1], 16)
                img_data[value1, value2] += 1  # 좌표 수정
            if len_check:
                break

        npy_list.append(img_data)

    return npy_list
