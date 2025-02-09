# This file will be configured to contain variables for CPack. These variables
# should be set in the CMake list file of the project before CPack module is
# included. The list of available CPACK_xxx variables and their associated
# documentation may be obtained using
#  cpack --help-variable-list
#
# Some variables are common to all generators (e.g. CPACK_PACKAGE_NAME)
# and some are specific to a generator
# (e.g. CPACK_NSIS_EXTRA_INSTALL_COMMANDS). The generator specific variables
# usually begin with CPACK_<GENNAME>_xxxx.


set(CPACK_BUILD_SOURCE_DIRS "D:/AndroidDevelopBasic/MoeTranslate/app/src/main/cpp;D:/AndroidDevelopBasic/MoeTranslate/app/.cxx/RelWithDebInfo/4q6y5t2i/arm64-v8a")
set(CPACK_CMAKE_GENERATOR "Ninja")
set(CPACK_COMPONENT_UNSPECIFIED_HIDDEN "TRUE")
set(CPACK_COMPONENT_UNSPECIFIED_REQUIRED "TRUE")
set(CPACK_DEBIAN_PACKAGE_MAINTAINER "Taku Kudo")
set(CPACK_DEFAULT_PACKAGE_DESCRIPTION_FILE "D:/AndroidSDK/cmake/3.22.1/share/cmake-3.22/Templates/CPack.GenericDescription.txt")
set(CPACK_DEFAULT_PACKAGE_DESCRIPTION_SUMMARY "sentencepiece built using CMake")
set(CPACK_GENERATOR "TXZ")
set(CPACK_IGNORE_FILES "/build/;/.git/;/dist/;/sdist/;~$;")
set(CPACK_INSTALLED_DIRECTORIES "D:/AndroidDevelopBasic/MoeTranslate/app/src/main/cpp;/")
set(CPACK_INSTALL_CMAKE_PROJECTS "")
set(CPACK_INSTALL_PREFIX "C:/Program Files (x86)/sentencepiece")
set(CPACK_MODULE_PATH "")
set(CPACK_NSIS_DISPLAY_NAME "sentencepiece 0.2.0")
set(CPACK_NSIS_INSTALLER_ICON_CODE "")
set(CPACK_NSIS_INSTALLER_MUI_ICON_CODE "")
set(CPACK_NSIS_INSTALL_ROOT "$PROGRAMFILES")
set(CPACK_NSIS_PACKAGE_NAME "sentencepiece 0.2.0")
set(CPACK_NSIS_UNINSTALL_NAME "Uninstall")
set(CPACK_OUTPUT_CONFIG_FILE "D:/AndroidDevelopBasic/MoeTranslate/app/.cxx/RelWithDebInfo/4q6y5t2i/arm64-v8a/CPackConfig.cmake")
set(CPACK_PACKAGE_CONTACT "taku@google.com")
set(CPACK_PACKAGE_DEFAULT_LOCATION "/")
set(CPACK_PACKAGE_DESCRIPTION_FILE "D:/AndroidSDK/cmake/3.22.1/share/cmake-3.22/Templates/CPack.GenericDescription.txt")
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "sentencepiece built using CMake")
set(CPACK_PACKAGE_FILE_NAME "sentencepiece-0.2.0-Source")
set(CPACK_PACKAGE_INSTALL_DIRECTORY "sentencepiece 0.2.0")
set(CPACK_PACKAGE_INSTALL_REGISTRY_KEY "sentencepiece 0.2.0")
set(CPACK_PACKAGE_NAME "sentencepiece")
set(CPACK_PACKAGE_RELOCATABLE "true")
set(CPACK_PACKAGE_VENDOR "Humanity")
set(CPACK_PACKAGE_VERSION "0.2.0")
set(CPACK_PACKAGE_VERSION_MAJOR "0")
set(CPACK_PACKAGE_VERSION_MINOR "2")
set(CPACK_PACKAGE_VERSION_PATCH "0")
set(CPACK_RESOURCE_FILE_LICENSE "D:/AndroidDevelopBasic/MoeTranslate/app/src/main/cpp/LICENSE")
set(CPACK_RESOURCE_FILE_README "D:/AndroidDevelopBasic/MoeTranslate/app/src/main/cpp/README.md")
set(CPACK_RESOURCE_FILE_WELCOME "D:/AndroidSDK/cmake/3.22.1/share/cmake-3.22/Templates/CPack.GenericWelcome.txt")
set(CPACK_RPM_PACKAGE_SOURCES "ON")
set(CPACK_SET_DESTDIR "OFF")
set(CPACK_SOURCE_GENERATOR "TXZ")
set(CPACK_SOURCE_IGNORE_FILES "/build/;/.git/;/dist/;/sdist/;~$;")
set(CPACK_SOURCE_INSTALLED_DIRECTORIES "D:/AndroidDevelopBasic/MoeTranslate/app/src/main/cpp;/")
set(CPACK_SOURCE_OUTPUT_CONFIG_FILE "D:/AndroidDevelopBasic/MoeTranslate/app/.cxx/RelWithDebInfo/4q6y5t2i/arm64-v8a/CPackSourceConfig.cmake")
set(CPACK_SOURCE_PACKAGE_FILE_NAME "sentencepiece-0.2.0-Source")
set(CPACK_SOURCE_TOPLEVEL_TAG "Android-Source")
set(CPACK_STRIP_FILES "")
set(CPACK_SYSTEM_NAME "Android")
set(CPACK_THREADS "1")
set(CPACK_TOPLEVEL_TAG "Android-Source")
set(CPACK_WIX_SIZEOF_VOID_P "8")

if(NOT CPACK_PROPERTIES_FILE)
  set(CPACK_PROPERTIES_FILE "D:/AndroidDevelopBasic/MoeTranslate/app/.cxx/RelWithDebInfo/4q6y5t2i/arm64-v8a/CPackProperties.cmake")
endif()

if(EXISTS ${CPACK_PROPERTIES_FILE})
  include(${CPACK_PROPERTIES_FILE})
endif()
