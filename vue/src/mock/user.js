import Mock from 'mockjs'

const NameList = []
const count = 10

for (let i = 0; i < count; i++) {
  NameList.push(Mock.mock({
    id: i,
    name: '@first',
    age: '@integer(0, 100)',
    email: '@email'
  }))
}

export default [
  // username search
  {
    url: '/bm-01/user/query_paged',
    type: 'get',
    response: () => {
      return {
        success: true,
        page: 1,
        row: 10,
        rows: NameList,
        total: 20,
        totalPage: 1
      }
    }
  },
  {
    url: '/bm-01/user/save_submit',
    type: 'post',
    response: () => {
      return {
        success: true,
        message: ''
      }
    }
  },
  {
    url: '/bm-01/user/delete_by_id',
    type: 'post',
    response: () => {
      return {
        success: true,
        message: ''
      }
    }
  },
  {
    url: '/restful/i18nRestful/language',
    type: 'get',
    response: () => {
      return ''
    }
  }
]
