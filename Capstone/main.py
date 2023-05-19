# parameter : 경로
# .txt 확장자/16진수
import java.io.IOException
import argparse
import sys
import process
import predict

import tensorflow as tf
from tensorflow.keras import layers
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from keras.applications.efficientnet_v2 import EfficientNetV2
from keras.applications.efficientnet_v2 import EfficientNetV2M
from tensorflow.keras.applications.efficientnet_v2 import preprocess_input

import pandas as pd
import os
import numpy as np
from tqdm import tqdm
import pickle
import csv
import math


def main(input_file_path, output_file_path, model):

    process.process_files(input_file_path, output_file_path)
    process.change_extension(output_file_path)
    npy_array = process.change_to_array(output_file_path)

    predict.predict_files(npy_array, model)


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
    # 파라미터 추출
    input_file_path = sys.argv[1]
    output_file_path = sys.argv[2]

    model = efficient_net()

    # main 함수 호출
    main(input_file_path, output_file_path, model)


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
