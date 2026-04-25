export function tagRender(h, proxy) {
  return (
    <span>
      {proxy.formatter.data.map(item => (
        <el-tag
          {...{attrs: proxy.$attrs}}
          {...{on: proxy.$listeners}}
        >
          {item}
        </el-tag>
      ))}
    </span>
  )
}
