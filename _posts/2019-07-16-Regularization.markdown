---
layout: post
title: "Regularization"
date: 2019-07-17 20:01:00
author: Sihyeon Kim
categories: deep-learning
---

# Regularization
Main purpose is to avoid overfitting.  
Overfitting: 학습 데이터를 너무 믿는 나머지 테스트 데이터를 못 맞추는 것이다.  

underline function을 알 수 없으므로 overfitting이 난다는 사실을 간접적으로만 알 수 있다.  

overfitting이 나는 이유는 random noise 때문이다.  

Regularization의 목적은 generalization이다.  

Overfitting을 막는 방법?  
(1) 많은 데이터를 모으자  
혹은 데이터 augmentation으로 뻥튀기 시켜야 한다.  

(2) use a model with the right capacity  

(3) average many different models (ensemble)  

(4) use dropout, dropconnect, or batchnorm (technic)  

Limiting capacity  
architecture: limit the number of hidden layers and units per layer  
early stopping: stop the learning before it overfits using validation sets  
weight-decay: penalize large weights using penalties or constraints on their squared values (L2 penalty) or absolute values (L1 penalty)  

batch normalization  
전체 배치에 평균을 빼고 variance로 나누어 주면 된다.  
benefits  
(1) learning rate을 늘려도 된다.  
(2) drop out을 쓰지 않아도 된다.
(3) L2 weight decay를 안 써도 잘 된다.  
(4) learning rate을 높여도 된다.  
(5) LRN을 안 써도 된다.  

어떤 문제를 풀든지 overfitting은 발생하므로 regularization을 사용한다.  
어떤 regularization이 좋은지는 감이 중요하다. 노하우가 중요하다. 예술의 영역에 가깝다.  

book review  
(1) parameter norm penalties  
(2) dataset augmentation  
(3) noise robustness: to input, weights, and output  
(4) semi-supervised learning = learning a representation  
(5) multitask learning  
(6) early stopping  
(7) parameter tying and parameter sharing   
(8) sparse representation  
(9) bagging and other ensemble methods  
(10) dropout  
(11) adversarial training  

(1) parameter norm penalties  
neural net의 weight가 너무 커지지 않도록 한다.  
제곱을 더하거나 절댓값을 더하는 방식이다.  

(2) data augmentation  
the best way to make a machine learning model generalize better is to train it on more data.  
of course, in practice, the amount of data we have is limited. one way to get around this problem is to create fake data and add it to the training set.  
GAN을 통해 data augmentation을 할 수 있다.  

(3) noise robustness  
in the general case, it is important to remember that noise injection can be much powerful than simply shrinking the parameters, especially when the noise is added to the hidden units.  

another way that noise has been used is by adding it to the weights.  

most datasets have some amount of mistakes in the y labels. label-smoothing can be used in this regard.  

(4) semi-supervised learning  
in the paradigm of semi-supervised learning, both unlabeled examples and labeled examples are used to.  

in the context of deep learning, semi-supervised learning usually refers to learning a representation.  
the goal is to learn a representation so that examples from the same class have similar representations.  
unsupervised learning can provide useful cues for how to group examples in representation space.  


CNN에서 feature extraction이 일종의 representation을 찾는 것이다.  
이미지에서 유용한 feature를 찾는 representation이다.  




### 참고할 논문

Dropout- A Simple Way to Prevent Neural Networks from Overfitting(2014)  
Batch Normalization- Accelerating Deep Network Training b y Reducing Internal Covariate Shift(2015)  

### 참고 자료  
Chapter 7 Regularization for Deep Learning by Ian Goodfellow

### 참고할 자료  
learning from data - caltech
