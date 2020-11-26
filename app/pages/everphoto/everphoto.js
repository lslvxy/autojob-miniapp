// pages/everphoto/everphoto.js
const app = getApp()

Page({

  /**
   * 页面的初始数据
   */
  data: {
    showTopTips: false,
    runTime: '00:00',
    formData: {
      time: '00:00'
    },
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
    }, {
      name: 'time',
      rules: {
        required: true,
        message: '请选择执行时间'
      },
    }]
  },
  formInputChange(e) {
    const {
      field
    } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: e.detail.value
    })
  },
  bindDateChange: function (e) {
    this.setData({
      runTime: e.detail.value,
      [`formData.time`]: e.detail.value
    })
  },
  submitForm(e) {
    var _this = this;
    this.selectComponent('#form').validate((valid, errors) => {
      console.log('valid', valid, errors)
      if (!valid) {
        const firstError = Object.keys(errors)
        if (firstError.length) {
          this.setData({
            error: errors[firstError[0]].message
          })

        }
      } else {
        // wx.showToast({
        //   title: '校验通过'
        // })
        wx.request({
          url: app.globalData.baseUrl + '/everphoto/create', //仅为示例，并非真实的接口地址
          method: 'POST',
          data: {
            userId: app.globalData.openId,
            account: _this.data.formData.account,
            password: _this.data.formData.password,
            hour: _this.data.formData.time.split(':')[0],
            mins: _this.data.formData.time.split(':')[1]
          },
          header: {
            'content-type': 'application/json' // 默认值
          },
          success(res) {
            wx.requestSubscribeMessage({
              tmplIds: ['j5OIz1iUpiBpx_80xtO0fmmc92gL0MFqU81GH2mTe_Y'],
              success(res) {

              }
            })
          }
        })
      }
    })
  },
  deleteAccount() {
    var _this = this;
    wx.request({
      url: app.globalData.baseUrl + '/everphoto/delete', //仅为示例，并非真实的接口地址
      method: 'POST',
      data: {
        userId: app.globalData.openId,
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        wx.navigateTo({
          url: '../main/main'
        })
      }
    })
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
    var _this = this;

    wx.request({
      url: app.globalData.baseUrl + '/everphoto/get', //仅为示例，并非真实的接口地址
      method: 'POST',
      data: {
        userId: app.globalData.openId,
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        var fd = {
          account: res.data.account,
          password: res.data.password,
          time: res.data.time
        }
        _this.setData({
          formData: fd,
          runTime: res.data.time,
        })
      }
    })
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  }
})