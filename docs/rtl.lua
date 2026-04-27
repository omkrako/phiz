-- Lua filter: sets RTL direction on all paragraphs and runs
local rtl_para = '<w:pPr><w:bidi/><w:jc w:val="right"/></w:pPr>'
local rtl_run  = '<w:rPr><w:rtl/></w:rPr>'

function Para(el)
  local bidi = pandoc.RawInline('openxml', '<w:rPr><w:rtl/></w:rPr>')
  local result = {}
  table.insert(result, pandoc.RawBlock('openxml',
    '<w:p><w:pPr><w:bidi/><w:jc w:val="right"/></w:pPr>'))
  for _, inline in ipairs(el.content) do
    table.insert(result, pandoc.Plain({inline}))
  end
  table.insert(result, pandoc.RawBlock('openxml', '</w:p>'))
  -- Simpler: inject bidi property via a surrounding rawblock
  return el
end

-- Inject bidi into every paragraph via RawBlock injection
function Pandoc(doc)
  local bidi_open  = '<w:pPr><w:bidi/><w:jc w:val="right"/></w:pPr>'
  local blocks = {}
  for _, block in ipairs(doc.blocks) do
    -- Wrap each paragraph with bidi markers
    if block.t == "Para" or block.t == "Plain" then
      table.insert(blocks, pandoc.RawBlock('openxml',
        '<w:p><w:pPr><w:bidi/><w:jc w:val="right"/></w:pPr><w:r><w:rPr><w:rtl/></w:rPr><w:t xml:space="preserve"> </w:t></w:r></w:p>'))
    end
    table.insert(blocks, block)
  end
  return pandoc.Pandoc(doc.blocks, doc.meta)
end
