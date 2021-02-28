package com.seriouslypro.pnpconvert

class NumberSequence {
    int index = 0

    NumberSequence(int first) {
        index = first
    }

    int next() {
        int id = index
        index++
        return id
    }
}