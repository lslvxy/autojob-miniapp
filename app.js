//app.js
App({
  onLaunch: function () {
    // 展示本地存储能力
    wx.cloud.init()
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)
    var _this = this;
    // 登录
    wx.login({
      success: res => {
        var value = wx.getStorageSync('openId')
        _this.globalData.openId = value;
        if (!value) {
          wx.cloud.callFunction({
            // 云函数名称
            name: 'login',
            // 传给云函数的参数
            success: function(res) {
              var openId=res.result.openid;
              console.log(res.result.openid) // 3
              wx.setStorageSync('openId', openId);
              _this.globalData.openId = openId;
            },
            fail: console.error
          })


          // wx.request({
          //   url: _this.globalData.baseUrl + '/user/getopenid', 
          //   data: {
          //     code: res.code,
          //   },
          //   header: {
          //     'content-type': 'application/json' // 默认值
          //   },
          //   success(res) {
          //     var openId = res.data;
          //     wx.setStorageSync('openId', openId);

          //     _this.globalData.openId = openId;
          //     // wx.request({
          //     //   url: _this.globalData.baseUrl + '/user/update', 
          //     //   data: {
          //     //     openId: openId,
          //     //     nickName: "",
          //     //     avatarUrl: ""
          //     //   },
          //     //   header: {
          //     //     'content-type': 'application/json' // 默认值
          //     //   },
          //     //   success(res) {

          //     //   }
          //     // })
          //     // 获取用户信息
          //     // wx.getSetting({
          //     //   success: res => {
          //     //     if (res.authSetting['scope.userInfo']) {
          //     //       // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
          //     //       wx.getUserInfo({
          //     //         success: res => {
          //     //           // 可以将 res 发送给后台解码出 unionId
          //     //           _this.globalData.userInfo = res.userInfo
          //     //           wx.request({
          //     //             url: _this.globalData.baseUrl + '/user/update', 
          //     //             data: {
          //     //               openId: openId,
          //     //               nickName: res.userInfo.nickName,
          //     //               avatarUrl: res.userInfo.avatarUrl
          //     //             },
          //     //             header: {
          //     //               'content-type': 'application/json' // 默认值
          //     //             },
          //     //             success(res) {

          //     //             }
          //     //           })
          //     //           // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
          //     //           // 所以此处加入 callback 以防止这种情况
          //     //           if (_this.userInfoReadyCallback) {
          //     //             _this.userInfoReadyCallback(res)
          //     //           }
          //     //         }
          //     //       })
          //     //     }
          //     //   }
          //     // })

          //   },
          //   fail(e) {
          //     wx.showToast({
          //       title: '服务器异常',
          //       icon: 'none',
          //       duration: 2000
          //     })
          //   }
          // }) // 发送 res.code 到后台换取 openId, sessionKey, unionId
        }
      }
    })
  },
   dateFormat:function(fmt, date) {
    let ret;
    const opt = {
        "Y+": date.getFullYear().toString(),        // 年
        "m+": (date.getMonth() + 1).toString(),     // 月
        "d+": date.getDate().toString(),            // 日
        "H+": date.getHours().toString(),           // 时
        "M+": date.getMinutes().toString(),         // 分
        "S+": date.getSeconds().toString()          // 秒
        // 有其他格式化字符需求可以继续添加，必须转化成字符串
    };
    for (let k in opt) {
        ret = new RegExp("(" + k + ")").exec(fmt);
        if (ret) {
            fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
        };
    };
    return fmt;
},
  globalData: {
    tmplIds: ['UYmCUg__IsjSMNPEhsHYx440P84NanoSS1fABW2WApw'],
    modules:{
    },
    userInfo: null,
    // baseUrl: 'http://127.0.0.1:8080'
    baseUrl: 'https://autojobs.laysan.site'

  }
})