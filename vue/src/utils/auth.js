
/* eslint-disable-next-line */
const permissions = UserContext.user.permissions

// 入库口权限code
const enterPrefix = '/wms/enter/site/permission/'
const enterPermissions = (permissions || []).filter(item => item.startsWith(enterPrefix))
const enterPermissionCodes = enterPermissions.map(item => item.replace(enterPrefix, '')).filter(item => !!item)

// 出库口权限code
const outPrefix = '/wms/out/site/permission/'
const outPermissions = (permissions || []).filter(item => item.startsWith(outPrefix))
const outPermissionCodes = outPermissions.map(item => item.replace(outPrefix, '')).filter(item => !!item)

export {
  permissions,
  enterPermissionCodes,
  outPermissionCodes
}
