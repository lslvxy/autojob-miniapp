// pages/everphoto/everphoto.js
const app = getApp()

Page({

  /**
   * 页面的初始数据
   */
  data: {
    showTopTips: false,
    sckey: ''
  },
  formInputChange(e) {
    this.setData({
      sckey: e.detail.value
    })
  },
  submitForm(e) {
    var _this = this;
    this.selectComponent('#form').validate((valid, errors) => {
      if (!valid) {
        const firstError = Object.keys(errors)
        if (firstError.length) {
          this.setData({
            error: errors[firstError[0]].message
          })
        }
      } else {
        wx.request({
          url: app.globalData.baseUrl + '/server/create',
          method: 'POST',
          data: {
            userId: app.globalData.openId,
            sckey: _this.data.sckey,
          },
          header: {
            'content-type': 'application/json' // 默认值
          },
          success(res) {
            wx.showToast({
              title: '配置成功'
            })
          }
        })
      }
    })
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    // wx.setClipboardData({
    //   data: 'http://sc.ftqq.com/',
    //   success(res) {
    //     wx.getClipboardData({
    //       success(res) {
    //         console.log(res.data) // data
    //       }
    //     })
    //   }
    // })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
    var _this = this;

    wx.request({
      url: app.globalData.baseUrl + '/server/get',
      method: 'POST',
      data: {
        userId: app.globalData.openId,
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        _this.setData({
          sckey: res.data.sckey,
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