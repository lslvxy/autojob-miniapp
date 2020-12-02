//app.js
App({
  onLaunch: function () {
    // 展示本地存储能力
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)
    var _this = this;
    // 登录
    wx.login({
      success: res => {
        wx.request({
          url: _this.globalData.baseUrl + '/user/getopenid', //仅为示例，并非真实的接口地址
          data: {
            code: res.code,
          },
          header: {
            'content-type': 'application/json' // 默认值
          },
          success(res) {
            var openId = res.data;
            _this.globalData.openId = openId;
            // 获取用户信息
            wx.getSetting({
              success: res => {
                if (res.authSetting['scope.userInfo']) {
                  // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
                  wx.getUserInfo({
                    success: res => {
                      // 可以将 res 发送给后台解码出 unionId
                      _this.globalData.userInfo = res.userInfo
                      wx.request({
                        url: _this.globalData.baseUrl + '/user/update', //仅为示例，并非真实的接口地址
                        data: {
                          openId: openId,
                          nickName: res.userInfo.nickName,
                          avatarUrl: res.userInfo.avatarUrl
                        },
                        header: {
                          'content-type': 'application/json' // 默认值
                        },
                        success(res) {

                        }
                      })
                      // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
                      // 所以此处加入 callback 以防止这种情况
                      if (_this.userInfoReadyCallback) {
                        _this.userInfoReadyCallback(res)
                      }
                    }
                  })
                }
              }
            })

          },
          fail(e) {
            wx.showToast({
              title: '服务器异常',
              icon: 'none',
              duration: 2000
            })
          }
        }) // 发送 res.code 到后台换取 openId, sessionKey, unionId
      }
    })
  },
  globalData: {
    tmplIds: ['UYmCUg__IsjSMNPEhsHYx440P84NanoSS1fABW2WApw'],
    openId: null,
    userInfo: null,
    // baseUrl: 'https://laisen.site'
    baseUrl: 'https://autojob.laysan.site'
    // baseUrl: 'http://192.168.1.6:8080'
    // baseUrl: 'http://47.96.23.144:8080'

  }
})