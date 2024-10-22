package com.raunakjodhawat.profile

object ProfileException {
  class ProfileAlreadyExistsException(name: String)
      extends Throwable(s"Profile '$name' already exists")
  class ProfileDoesNotExistException(name: String)
      extends Throwable(s"Profile '$name' does not exist")
  class ProfileDefaultDeleteException(name: String)
      extends Throwable(
        s"Profile '$name' can't be deleted, as it's the default"
      )
}
