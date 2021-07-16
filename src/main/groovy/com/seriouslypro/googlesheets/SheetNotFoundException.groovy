package com.seriouslypro.googlesheets

class SheetNotFoundException extends RuntimeException {
    SheetNotFoundException(String title) {
        new RuntimeException("Sheet not found, title: ${title}")
    }
}
