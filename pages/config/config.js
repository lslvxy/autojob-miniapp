// pages/config/config.js
const app = getApp()

Page({

  /**
   * Page initial data
   */
  data: {
    type: '',
    showTopTips: false,
    accountList: [{}],
    rules: [{
      name: 'account',
      rules: {
        required: true,
        message: '请输入账号'
      },
    }, {
      name: 'password',
      rules: {
        required: true,
        message: '请输入密码'
      },
    }]
  },
  getAccount(id) {
    var list = this.data.accountList;
    for (var i = 0; i < list.length; i++) {
      if (list[i].id == id) {
        return {
          index: i,
          account: list[i]
        }
      }
    }
  },

  formInputChange(e) {
    var accountId = e.target.dataset.id;
    var {
      index,
      account
    } = this.getAccount(accountId);
    account[e.target.dataset.field] = e.detail.value;
    var list = this.data.accountList;
    list.splice(index, 1, account)
    this.setData({
      accountList: list
    })
  },
  submitForm(e) {
    var _this = this;
    var list = this.data.accountList;
    var hasError = false;
    for (var i = 0; i < list.length; i++) {
      var _id = list[i]._id;
      this.selectComponent('#form_' + _id).validate((valid, errors) => {
        if (!valid) {
          const firstError = Object.keys(errors)
          if (firstError.length) {
            this.setData({
              error: errors[firstError[0]].message
            })
            hasError = true;
            return false
          }
        }
      });
      if (hasError) {
        return false
      }
    }

    wx.request({
      url: app.globalData.baseUrl + '/account/create', 
      method: 'POST',
      data: {
        userId: app.globalData.openId,
        type: _this.data.type,
        accountList: _this.data.accountList
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        wx.showToast({
          title: '配置成功'
        })
        // wx.requestSubscribeMessage({
        //   tmplIds: app.globalData.tmplIds,
        //   success(res) {
        //   }
        // })
      }
    })
  },
  addAccount() {
    var _this = this;
    var newList = _this.data.accountList;
    if (newList.length >= 5) {
      wx.showToast({
        title: '最多5个账号'
      })
      return;
    }
    this.setData({
      accountList: [...newList, {
        "_id": this.create_UUID(),
        "id":"",
        "account":"",
        "password":"",
        "time":"00:00",
      }]
    })
  },
  deleteAccount(e) {
    var accountId = e.target.dataset.id;

    var _this = this;
    wx.request({
      url: app.globalData.baseUrl + '/account/delete',
      method: 'POST',
      data: {
        id: accountId,
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        wx.navigateTo({
          url: '../config/config?type=' + _this.data.type
        })
      }
    })
  },
  create_UUID: function () {
    var dt = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = (dt + Math.random() * 16) % 16 | 0;
      dt = Math.floor(dt / 16);
      return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
    return uuid;
  },
  /**
   * Lifecycle function--Called when page load
   */
  onLoad: function (options) {
    var type = options.type;
    this.setData({
      type: type
    });
    wx.setNavigationBarTitle({
      title: app.globalData.modules[type] //页面标题为路由参数
    })
  },

  /**
   * Lifecycle function--Called when page is initially rendered
   */
  onReady: function () {
    var _this = this;

    wx.request({
      url: app.globalData.baseUrl + '/account/get', 
      method: 'POST',
      data: {
        userId: app.globalData.openId,
        type: _this.data.type
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        _this.setData({
          accountList: res.data
        })
      }
    })
  },

  /**
   * Lifecycle function--Called when page show
   */
  onShow: function () {

  },

  /**
   * Lifecycle function--Called when page hide
   */
  onHide: function () {

  },

  /**
   * Lifecycle function--Called when page unload
   */
  onUnload: function () {

  },

  /**
   * Page event handler function--Called when user drop down
   */
  onPullDownRefresh: function () {

  },

  /**
   * Called when page reach bottom
   */
  onReachBottom: function () {

  },

  /**
   * Called when user click on the top right corner to share
   */
  onShareAppMessage: function () {

  }
})