// pages/main/main.js
const app = getApp()

Page({

  /**
   * 页面的初始数据
   */
  data: {
    typeList: [{
        name: '时光相册',
        type: 'everphoto',
        icon: "/assets/sg.jpeg"
      },
      {
        type: 'cloud189',
        name: '天翼云盘',
        icon: "/assets/ty.png"
      },
    ]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var _this = this;
    wx.request({
      url: app.globalData.baseUrl + '/sys/typeList',
      header: {
        'content-type': 'application/json' // 默认值
      },
      success(res) {
        if (res.statusCode != 200) {
          return false;
        }
        var typeList = res.data;
        _this.setData({
          typeList: typeList,
        })
        var mods = {};
        typeList.forEach(v => {
          mods[v.type] = v.name;
        });
        app.globalData.modules = {
          ...mods
        };

      }
    })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

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