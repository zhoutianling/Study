cmake_minimum_required(VERSION 3.18.1)

project("memory_stress")

add_library(
        memory_stress
        SHARED
        memory_stress.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        memory_stress
        ${log-lib})