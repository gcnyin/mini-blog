package gcnyin.blog

import gcnyin.blog.common.Dto.{Post, PostWithoutContent}
import scalatags.Text
import scalatags.Text.all._

object HTMLEngine {
  def get404Page: String =
    <html>
      <head>
        <title>404 | Mini Blog</title>
      </head>
      <body>
        <p>Resource not found</p>
      </body>
    </html>.toString

  def getHomePage(posts: Seq[PostWithoutContent]): Text.TypedTag[String] =
    html(cls := "h-100")(
      getNav,
      getHeader("Home"),
      body(cls := "d-flex flex-column h-100")(
        tag("main")(cls := "flex-shrink-0 container")(
          h1("Mini Blog"),
          for (post <- posts)
            yield p(
              a(href := s"/posts?postId=${post.id}")(post.title)
            )
        ),
        footerCode
      )
    )

  def getPostDetailPage(post: Post): Text.TypedTag[String] =
    html(cls := "h-100")(
      getNav,
      getHeader(post.title),
      body(cls := "d-flex flex-column h-100")(
        tag("main")(cls := "flex-shrink-0 container")(
          h1(post.title),
          for (pa <- post.content.split("\n")) yield p(pa)
        ),
        footerCode
      )
    )

  private def footerCode: Text.TypedTag[String] =
    tag("footer")(cls := "footer mt-auto py-3 bg-light")(
      script(href := "https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"),
      script(href := "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.min.js"),
      div(cls := "container")(
        span(cls := "text-muted")("Powered by Scala, Akka, Zio, Cats, Scalatags and MongoDB"),
        br(),
        span(
          "© 2021 ",
          a(href := "https://github.com/gcnyin/mini-blog")("gcnyin")
        )
      )
    )

  private def getNav: Text.TypedTag[String] =
    tag("nav")(cls := "navbar navbar-expand-lg navbar-light bg-light")(
      div(cls := "container")(
        a(cls := "navbar-brand", href := "/")("Blog"),
        button(cls := "navbar-toggler", `type` := "Button")(
          span(cls := "navbar-toggler-icon")
        )
      )
    )

  private def getHeader(t: String): Text.TypedTag[String] =
    head(
      title := s"$t | Mini Blog",
      link(
        href := "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css",
        attr("rel") := "stylesheet",
        crossorigin := "anonymous"
      )
    )
}
