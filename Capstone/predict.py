import tensorflow as tf
from tensorflow.keras import layers
from tensorflow.keras.preprocessing.image import ImageDataGenerator

import pandas as pd
import os
import numpy as np
from tqdm import tqdm
import pickle
import csv
import math

from tensorflow.python.client import device_lib


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


def generate_predict_data(images_dict, batch_size):
    file_names_list = list(images_dict.keys())
    for i in range(0, len(file_names_list), batch_size):
        batch_file_names = file_names_list[i:i + batch_size]
        batch_images = [images_dict[file_name]
                        for file_name in batch_file_names]
        max_shape = tuple(max(img.shape[i]
                          for img in batch_images) for i in range(2))
        padded_batch_images = [np.pad(img, ((
            0, max_shape[0] - img.shape[0]), (0, max_shape[1] - img.shape[1]))) for img in batch_images]
        # 이미지에 채널 축 추가
        yield batch_file_names, np.stack(padded_batch_images, axis=0)[..., np.newaxis]


def make_generator(output_file_path):
    file_names = [f for f in os.listdir(
        output_file_path) if f.endswith('.npy')]
    file_name_to_image = {file_name: np.load(os.path.join(
        output_file_path, file_name)) for file_name in file_names}

    batch_size = 1
    num_predictions = len(file_names)
    # 예측 데이터 제너레이터 생성
    predict_data_generator = generate_predict_data(
        file_name_to_image, batch_size)
    return predict_data_generator, num_predictions


def save_csv(predictions):
    # CSV 파일에 결과 저장
    with open('./predictions.csv', 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Id'] + [f'Prediction{i}' for i in range(1, 10)])
        for file_name, predicted_class in predictions.items():
            # One-hot encoding 생성
            one_hot = [0] * 9
            one_hot[predicted_class - 1] = 1
            csv_writer.writerow([file_name] + one_hot)


def predict_files(output_file_path, model):

    use_gpu()

    predict_data_generator, num_predictions = make_generator(output_file_path)
    predictions = {}  # 딕셔너리로 변경

    num_batches = math.ceil(num_predictions)

    for _ in tqdm(range(num_batches)):
        batch_file_names, batch_images = next(predict_data_generator)
        batch_predictions = model.predict(batch_images, verbose=0)
        predicted_classes = np.argmax(batch_predictions, axis=1)

        for file_name, predicted_class in zip(batch_file_names, predicted_classes):
            # 중복 예측을 방지하기 위해 딕셔너리에 이미 존재하는지 확인
            if file_name not in predictions:
                predictions[file_name.replace('.npy', '')] = predicted_class

    save_csv(predictions)
