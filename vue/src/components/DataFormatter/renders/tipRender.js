export function tipRender(h, proxy) {
  return (
    <span>
      <el-tooltip
        {...{attrs: proxy.$attrs}}
        {...{on: proxy.$listeners}}
      >
        <span>
          {proxy.formatter.data}
        </span>
      </el-tooltip>
    </span>
  )
}
