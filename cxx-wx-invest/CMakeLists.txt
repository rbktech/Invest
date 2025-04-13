cmake_minimum_required(VERSION 3.10)

file(STRINGS "version" VERSION)

project(wxInvest VERSION ${VERSION})

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_VERBOSE_MAKEFILE OFF)

set(wxWidgets_LIB_DIR $ENV{WXWIN}/lib/gcc_libx64)

find_package(wxWidgets 3.2.1 REQUIRED core base)

include_directories($ENV{ZLIB}/include)
include_directories($ENV{GPPanel}/include)
include_directories(${wxWidgets_INCLUDE_DIRS})

add_link_options(-static-libgcc)
add_link_options(-static-libstdc++)

link_directories(${wxWidgets_LIB_DIR})
link_directories($ENV{ZLIB}/lib)
link_directories($ENV{GPPanel}/lib/gcc_libx64)

add_executable(${PROJECT_NAME})

set_target_properties(${PROJECT_NAME}
        PROPERTIES
        OUTPUT_NAME Invest-${CMAKE_BUILD_TYPE}-${PROJECT_VERSION}
)

target_sources(${PROJECT_NAME} PRIVATE src/main.cpp)
target_sources(${PROJECT_NAME} PRIVATE src/mainframe.cpp)
target_sources(${PROJECT_NAME} PRIVATE src/drawpane.cpp)

target_link_libraries(${PROJECT_NAME} zlibstatic)
target_link_libraries(${PROJECT_NAME} gpPanel)
target_link_libraries(${PROJECT_NAME} ${wxWidgets_LIBRARIES})

# ------------------------------------------------------------------------

get_filename_component(PARENT_DIR ../ ABSOLUTE)

add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy
        "${PARENT_DIR}/resources/BBG000BBJQV0_2022.zip"
        "${PROJECT_BINARY_DIR}/BBG000BBJQV0_2022.zip"
)