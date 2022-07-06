rm(list=ls())

library(pracma)
library(dplyr)

distance <- function(space, pom) {
  d1 <- sqrt((space[1,]$X - pom[1,]$X)^2 + (space[1,]$Y - pom[1,]$Y)^2)
  d2 <- sqrt((space[2,]$X - pom[1,]$X)^2 + (space[2,]$Y - pom[1,]$Y)^2)
  if (d1 > d2) {
    d <- d2
  } else {
    d <- d1
  }
  return(d)
}

flag <- 0
if (flag == 0) {
  loc <- read.csv("qwerty-hd.csv", header = TRUE, sep = " ")
} else if (flag == 1) {
  loc <- read.csv("typewise.csv", header = TRUE, sep = " ")
} else {
  loc <- read.csv("custom-fhd.csv", header = TRUE, sep = " ")
}

for (i in 1:length(loc$X)) {
  if (is.na(loc[i,1])) {
  } else {
    if (strcmp(loc[i,]$LETTER, c(""))) {
      loc <- loc[-c(i),]
    }
  }
}

prob <- read.csv("letters.csv", header = TRUE, sep = " ")
total <- prob[length(prob) - 1, length(prob)] * 1.0
prob <- prob[, 3:length(prob) - 1]
prob <- prob[1:length(prob$A) - 1,]

data <- read.csv("FittsTouch eksperimenti\\Josip\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data2 <- read.csv("FittsTouch eksperimenti\\Ana\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data3 <- read.csv("FittsTouch eksperimenti\\Dario\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data4 <- read.csv("FittsTouch eksperimenti\\Ingo\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data5 <- read.csv("FittsTouch eksperimenti\\Filip\\FittsTouch-P01-S01-B02-G01-C01-2D.sd1", header = TRUE, sep = ",")
data6 <- read.csv("FittsTouch eksperimenti\\Mario\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data7 <- read.csv("FittsTouch eksperimenti\\Sibil\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data8 <- read.csv("FittsTouch eksperimenti\\Karlo\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")
data9 <- read.csv("FittsTouch eksperimenti\\Antonio\\FittsTouch-P01-S01-B01-G01-C01-2D.sd1", header = TRUE, sep = ",")

data10 <- rbind(data, data2)
data11 <- rbind(data10, data3)
data12 <- rbind(data11, data4)
data13 <- rbind(data12, data5)
data14 <- rbind(data13, data6)
data15 <- rbind(data14, data7)
data16 <- rbind(data15, data8)
data17 <- rbind(data16, data9)

a1 <- data17$A
w <- data17$W
MT <- data17$MT.ms. / 1000
ID <- log2(a1 / w + 1)
model <- lm(MT ~ ID)

a <- as.numeric(model$coefficients[1])
b <- as.numeric(model$coefficients[2])

# a <- 0.1154 #iz literature
# b <- 0.1098

for (i in 1:length(prob)) {
  for (j in 1:length(prob)) {
    if (is.na(prob[i, j])) {
    } else {
      if (prob[i, j] > 0) {
        prob[i, j] <- prob[i, j] / total
      } else {
        prob[i, j] = 0
      }
    }
  }
}

MT_max <- matrix(0, nrow = length(loc$X), ncol = length(loc$X))

for (i in 1:length(loc$X)) {
  for (j in 1:length(loc$X)) {
    d <- 0
    pom <- NA
    if (loc[j,]$LETTER == "_" && loc[i,]$LETTER == "_") next
    d <- sqrt((loc[j,]$X - loc[i,]$X)^2 + (loc[j,]$Y - loc[i,]$Y)^2)
    ID_ij <- log2(d / (loc[j,]$W) + 1)
    MT_max[i, j] <- a + b * ID_ij
  }
}

ct <- 0.0
let <- loc$LETTER

ovo <- 0

for (i in 1:length(MT_max[1,])) {
  for (j in 1:length(MT_max[1,])) {
    count <- 0
    first <- let[i]
    second <- let[j]
    first_index <- 0
    second_index <- 0
    pom <- 96
    
    if(strcmp(first, "_")) {
      first_index = length(prob)
      count <- count + 1
    }

    if(strcmp(second, "_")) {
      second_index = length(prob)
      count <- count + 1
    }
    
    
    if (count < 2) {
      for (k in 1:length(prob) - 1) {
        if(pom + k == utf8ToInt(first)) {
          first_index = as.integer(k)
          count <- count + 1
        }
        
        if(pom + k == utf8ToInt(second)) {
          second_index = as.integer(k)
          count <- count + 1
        }
        
        if (count == 2) break
      }
    }
    
    ct <- ct + prob[first_index, second_index] * (MT_max[i, j])
  }
  
  CPS_max <- 1.0 / ct
  
  WPM_max <- CPS_max * (60 / 5)
}