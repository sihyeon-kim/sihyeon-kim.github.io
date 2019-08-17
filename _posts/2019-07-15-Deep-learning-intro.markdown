---
layout: post
title: "Deep learning introduction"
date: 2019-07-15 20:01:00
author: Sihyeon Kim
categories: deep-learning
---

# Basic single layer network
Fully connected layer, dense layer, Multi-layer perceptron  
m dimensional space에서 n dimensional space로 넘어가는 non linear mapping을 찾는 것  
행렬 m x n을 곱하면 된다.  
affine transfrom이다.  
여기에 activation function을 통과시켜 이 매핑에 non linearity를 준다.  
activation function이 없으면 즉 비선형성을 주지 않으면 몇 개의 레이어를 쌓아도 소용이 없다.  
비선형성을 주어야 네트워크가 복잡한 함수도 표현할 수 있다.  

# Activation Function
Sigmoid: 0과 1사의 값을 낼 때, 확률  
tanh: 음수가 필요할 때  
ReLU: 분류 네트워크에서 좋은 성능  
softplus: regression에 장점이 있다. 

# Epoch, batch size, iteration
one epoch: one forward and backward pass of all training data  
batch size: the number of training examples in one forward and backward pass  
one iteration: 전체 데이터를 배치 사이즈로 나눈 것, number of passes  

# Cost function  
this is just a function that we want to minimize  
there is no guarantee that it will bring us to the best solution  
it should be differentiable  
미분이 안 될때 reinforcement algorithm을 쓰면 학습이 가능하다. (자연어 처리에서 많이 쓰는 방법이다.)  


### 참고자료  

[1] [edwith](https://www.edwith.org/deeplearningchoi/joinLectures/10979)
