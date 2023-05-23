
import argparse
import sys
import process
import predict
import test

import tensorflow as tf
from tensorflow.keras import layers
from tensorflow.keras.preprocessing.image import ImageDataGenerator
# from keras.applications.efficientnet_v2 import EfficientNetV2
from keras.applications.efficientnet_v2 import EfficientNetV2M
from tensorflow.keras.applications.efficientnet_v2 import preprocess_input
from tensorflow.python.client import device_lib


import pandas as pd
import os
import numpy as np
from tqdm import tqdm
import pickle
import csv
import math


def use_gpu():
    print("Device_lib : " + str(device_lib.list_local_devices()))
    # '0번' GPU 사용
    print("Use able device : " + str(tf.config.experimental.list_physical_devices()))

    os.environ["CUDA_VISIBLE_DEVICES"] = "0, 1, 2, 3"
    gpus = tf.config.experimental.list_physical_devices('GPU')
    print(gpus)
    tf.config.experimental.set_visible_devices(gpus, 'GPU')

    # os.environ["CUDA_VISIBLE_DEVICES"]="0"
    # gpus = tf.config.experimental.list_physical_devices('GPU')
    # tf.config.experimental.set_visible_devices([],'GPU')

    if len(gpus) > 0:
        tf.config.experimental.set_memory_growth(gpus[0], True)
        print('GPU 메모리 할당 제한이 해제되었습니다.')
    else:
        print('GPU를 사용할 수 없습니다.')

    if len(gpus) > 0:
        tf.config.set_logical_device_configuration(
            gpus[0], [tf.config.LogicalDeviceConfiguration(memory_limit=1)])
        print('GPU 메모리 할당 비율이 조정되었습니다.')
    else:
        print('GPU를 사용할 수 없습니다.')

    logical_devices = tf.config.list_logical_devices('GPU')
    physical_devices = tf.config.list_physical_devices('GPU')

    if len(physical_devices) > 0:
        print('사용 가능한 GPU가 있습니다')
    else:
        print('사용 가능한 GPU가 없습니다')

    if len(logical_devices) > 0:
        print(logical_devices)
        print('GPU 사용 중입니다.')
    else:
        print('GPU를 사용하고 있지 않습니다.')
    print(tf.config.list_logical_devices())
    print(tf.config.experimental.list_physical_devices())
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
    # os.environ["CUDA_VISIBLE_DEVICES"] = "-1" => cpu 사용 강제
    tf.debugging.set_log_device_placement(True)


def main(input_file_path, file_to_npy, output_file_path, model):
    try:
        process.process_files(input_file_path, output_file_path)
    except Exception as e:
        print("process error", e)
    try:
        process.change_extension(output_file_path)
    except Exception as e:
        print('change extension error', e)
    try:
        process.change_to_array(output_file_path, file_to_npy)
    except Exception as e:
        print('change array error', e)
    try:
        predict.predict_files(output_file_path, file_to_npy, model)
    except Exception as e:
        print('predict error : ', e)


def efficient_net():
    # EfficientNetV2 모델 생성
    num_classes = 10  # 분류할 클래스 수
    # EfficientNetV2M_weights의 num_class 변수는 10으로 설정
    # EfficientNetV2M_weights_V2의 num_class 변수는 9

    input_shape = (None, None, 1)  # 입력 이미지 크기

    base_model = EfficientNetV2M(
        include_top=False,
        weights=None,
        input_tensor=None,
        input_shape=input_shape,
        pooling=None,
        classes=num_classes,
        classifier_activation="softmax"
    )

    inputs = tf.keras.Input(shape=input_shape)
    x = base_model(inputs)
    x = layers.GlobalAveragePooling2D()(x)
    outputs = layers.Dense(num_classes, activation='softmax')(x)

    model = tf.keras.Model(inputs, outputs)

    # 모델 컴파일
    model.compile(
        optimizer='Nadam',
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )
    model.load_weights('./EfficientNetV2M_weights.h5')

    return model


if __name__ == "__main__":
    use_gpu()
    # 파라미터 추출
    input_file_path = sys.argv[1]
    file_to_npy = sys.argv[2]
    output_file_path = sys.argv[3]

    # 출력을 파일로 저장할 파일 경로
    print_file_path = "./output.txt"

    # 출력을 파일로 리다이렉션
    sys.stdout = open(print_file_path, "w")

    try:
        model = efficient_net()
    except Exception as e:
        print('model error', e)

    # main 함수 호출
    main(input_file_path, file_to_npy, output_file_path, model)
    print('Done!')


# public class Main {
#     public static void main(String[] args) {
#         try {
#             // 실행할 명령어 생성
#             String[] command = {"python", "main.py", "읽어들일 파일 경로", "저장할 파일 경로"}

#             // 명령어 실행
#             Process process = Runtime.getRuntime().exec(command)

#             // 출력 읽기
#             BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
#             String line
#             while ((line=reader.readLine()) != null) {
#                 System.out.println(line)
#             }
#         } catch(IOException e) {
#             e.printStackTrace()
#         }
#     }
# }
