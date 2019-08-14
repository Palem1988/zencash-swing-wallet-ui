export BUILD_PATH=./build/native
export JARS_PATH=./build/jars
export NAME=HorizenDesktopGUIWallet
export MAIN_CLASS=com.vaklinov.zcashui.HorizenUI
export MAIN_JAR=ZENCashSwingWalletUI.jar
export PNG_ICON=./zencash-wallet-swing/src/icons/ZENCashWallet.iconset/icon_128x128.png
export ICNS_ICON=./zencash-wallet-swing/src/icons/ZENCashWallet.icns
export ICO_ICON=./zencash-wallet-swing/src/icons/ZEN.ico
export VERSION=$(if [[ $TRAVIS_TAG != $TRAVIS_BRANCH && $TRAVIS_TAG ]]; then echo $TRAVIS_TAG; else echo "0.1.0"; fi)
export PACKAGE_TYPE=$(if [[ $TRAVIS_OS_NAME == "osx" ]]; then echo dmg; elif [[ $TRAVIS_OS_NAME == "windows" ]]; then echo exe; else echo deb; fi)
export MAC_PARAMS=$(if [[ $TRAVIS_OS_NAME == "osx" ]]; then echo "--mac-bundle-identifier $MAIN_CLASS --icon $ICNS_ICON"; fi)
export LINUX_PARAMS=$(if [[ $TRAVIS_OS_NAME == "linux" ]]; then echo "--icon $PNG_ICON"; fi)
export WINDOWS_PARAMS=$(if [[ $TRAVIS_OS_NAME == "windows" ]]; then echo "--icon $ICO_ICON"; fi)